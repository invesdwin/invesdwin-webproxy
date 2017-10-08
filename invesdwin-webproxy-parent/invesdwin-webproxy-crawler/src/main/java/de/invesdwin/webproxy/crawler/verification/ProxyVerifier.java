package de.invesdwin.webproxy.crawler.verification;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import javax.inject.Named;

import de.invesdwin.context.log.Log;
import de.invesdwin.util.concurrent.Executors;
import de.invesdwin.util.concurrent.Futures;
import de.invesdwin.webproxy.ProxyVerification;
import de.invesdwin.webproxy.WebproxyProperties;
import de.invesdwin.webproxy.broker.contract.IBrokerService;
import de.invesdwin.webproxy.broker.contract.schema.BrokerRequest.ProcessResultFromCrawlerRequest;
import de.invesdwin.webproxy.broker.contract.schema.Proxy;
import de.invesdwin.webproxy.broker.contract.schema.RawProxy;
import de.invesdwin.webproxy.portscan.contract.IPortscanService;
import de.invesdwin.webproxy.portscan.contract.schema.PortscanAsyncRequest.PingRequest;

@ThreadSafe
@Named
public class ProxyVerifier {

    private final Log log = new Log(this);
    private final ExecutorService executor = Executors.newFixedThreadPool(getClass().getSimpleName(),
            WebproxyProperties.MAX_PARALLEL_DOWNLOADS);

    @Inject
    private IBrokerService broker;
    @Inject
    private IPortscanService portscan;
    @Inject
    private ProxyVerifierDataEnricher enricher;
    @Inject
    private ProxyVerification proxyVeri;

    public void verify(final Set<RawProxy> rawProxies) {
        final ProcessResultFromCrawlerRequest request = new ProcessResultFromCrawlerRequest();
        final List<Worker> workers = new ArrayList<Worker>();
        for (final RawProxy rawProxy : rawProxies) {
            workers.add(new Worker(rawProxy, request));
        }
        try {
            Futures.submitAndWait(executor, workers);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        proxyVeri.getStatisticsCallback().logFinalReport();
        proxyVeri.getStatisticsCallback().reset();
        log.info("%s proxies successfully and %s unsuccessfully verified, transmitting the result now",
                request.getSuccessfullyVerifiedProxies().size(), request.getUnsuccessfullyVerifiedProxies().size());
        broker.processResultFromCrawler(request);
    }

    /******************************* private *************************************/

    @ThreadSafe
    private class Worker implements Runnable {

        private final RawProxy rawProxy;
        @GuardedBy("request")
        private final ProcessResultFromCrawlerRequest request;

        Worker(final RawProxy rawProxy, final ProcessResultFromCrawlerRequest request) {
            this.rawProxy = rawProxy;
            this.request = request;
        }

        @Override
        public void run() {
            try {
                if (rawProxy.getPort() == null) {
                    //If just the host is specified, first ping the host
                    final PingRequest pingRequest = new PingRequest();
                    pingRequest.setToBePingedHost(rawProxy.getHost());
                    portscan.ping(pingRequest);
                } else {
                    //If host and port is specified, do a proxy verification
                    final Proxy proxy = enricher.enrich(rawProxy);
                    if (proxy != null) {
                        log.info("Proxy found: %s", proxy);
                        synchronized (request) {
                            request.getSuccessfullyVerifiedProxies().add(proxy);
                        }
                    } else {
                        synchronized (request) {
                            request.getUnsuccessfullyVerifiedProxies().add(rawProxy);
                        }
                    }
                }
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

    }

}
