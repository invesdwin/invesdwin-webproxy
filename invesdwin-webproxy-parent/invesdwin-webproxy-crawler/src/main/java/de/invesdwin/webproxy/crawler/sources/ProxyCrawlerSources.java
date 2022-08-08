package de.invesdwin.webproxy.crawler.sources;

import java.util.List;
import java.util.Set;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import javax.inject.Named;

import de.invesdwin.context.log.Log;
import de.invesdwin.context.log.error.Err;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.collections.Iterables;
import de.invesdwin.webproxy.broker.contract.BrokerContractProperties;
import de.invesdwin.webproxy.broker.contract.IBrokerService;
import de.invesdwin.webproxy.broker.contract.schema.BrokerRequest.AddToBeVerifiedProxiesRequest;
import de.invesdwin.webproxy.broker.contract.schema.RawProxy;

@Named
@ThreadSafe
public class ProxyCrawlerSources {

    private final Log log = new Log(this);

    @Inject
    private IProxyCrawlerSource[] crawlers;
    @Inject
    private IBrokerService broker;

    @GuardedBy("this")
    private boolean alreadyCrawling;

    public void crawlProxies() throws InterruptedException {
        synchronized (this) {
            Assertions.assertThat(alreadyCrawling)
                    .as("It should not happen that the signal for crawling is given more than once at the same time!")
                    .isFalse();
            alreadyCrawling = true;
        }

        try {
            for (final IProxyCrawlerSource crawler : crawlers) {
                try {
                    final Set<RawProxy> toBeVerifiedProxies = crawler.getRawProxies();
                    if (toBeVerifiedProxies != null) {
                        logFoundPossibleProxies(crawler, toBeVerifiedProxies);
                        if (toBeVerifiedProxies.size() > 0) {
                            final Iterable<List<RawProxy>> packets = Iterables.partition(toBeVerifiedProxies,
                                    BrokerContractProperties.MAX_PROXIES_PER_TASK);
                            for (final List<RawProxy> packet : packets) {
                                final AddToBeVerifiedProxiesRequest request = new AddToBeVerifiedProxiesRequest();
                                request.getToBeVerifiedProxies().addAll(packet);
                                broker.addToBeVerifiedProxies(request);
                            }
                        }
                    }
                } catch (final InterruptedException e) {
                    throw e;
                } catch (final Throwable e) {
                    Err.process(e);
                }
            }
        } finally {
            synchronized (this) {
                alreadyCrawling = false;
            }
        }
    }

    private void logFoundPossibleProxies(final IProxyCrawlerSource crawler, final Set<RawProxy> toBeVerifiedProxies) {
        if (log.isInfoEnabled()) {
            String message = "Found " + toBeVerifiedProxies.size() + " possible proxies from "
                    + crawler.getClass().getName() + ".";
            if (toBeVerifiedProxies.size() > 0) {
                message += " Transmitting those to the broker to spread the verification tasks.";
            }
            log.info(message);
        }
    }
}
