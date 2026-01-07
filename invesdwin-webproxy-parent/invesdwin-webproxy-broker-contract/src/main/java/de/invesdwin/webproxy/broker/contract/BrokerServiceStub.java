package de.invesdwin.webproxy.broker.contract;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.concurrent.ThreadSafe;

import de.invesdwin.context.beans.init.locations.PositionedResource;
import de.invesdwin.context.test.ATest;
import de.invesdwin.context.test.ITestContextSetup;
import de.invesdwin.context.test.stub.StubSupport;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.collections.Collections;
import de.invesdwin.util.collections.Iterables;
import de.invesdwin.webproxy.broker.contract.internal.BrokerContextLocation;
import de.invesdwin.webproxy.broker.contract.schema.BrokerRequest.AddToBeVerifiedProxiesRequest;
import de.invesdwin.webproxy.broker.contract.schema.BrokerRequest.ProcessResultFromCrawlerRequest;
import de.invesdwin.webproxy.broker.contract.schema.BrokerResponse.GetTaskForCrawlerResponse;
import de.invesdwin.webproxy.broker.contract.schema.BrokerResponse.GetWorkingProxiesResponse;
import de.invesdwin.webproxy.broker.contract.schema.Proxy;
import de.invesdwin.webproxy.broker.contract.schema.RawProxy;
import jakarta.inject.Named;

@Named
@ThreadSafe
public class BrokerServiceStub extends StubSupport implements IBrokerService {

    private static volatile boolean enabled = true;

    private static volatile boolean crawlForProxies;

    private final Map<RawProxy, Proxy> proxies = new ConcurrentHashMap<RawProxy, Proxy>();
    private final Set<RawProxy> rawProxies = Collections.newSetFromMap(new ConcurrentHashMap<RawProxy, Boolean>());

    public static void setCrawlForProxies(final boolean value) {
        crawlForProxies = value;
    }

    @Override
    public void setUpContextLocations(final ATest test, final List<PositionedResource> locations) throws Exception {
        if (!enabled) {
            if (!locations.remove(BrokerContextLocation.SERVICE_CONTEXT)) {
                Assertions.checkTrue(locations.remove(BrokerContextLocation.SERVICE_CONTEXT_FALLBACK));
            }
            locations.add(BrokerContextLocation.CLIENT_CONTEXT);
        }
    }

    @Override
    public void setUpContext(final ATest test, final ITestContextSetup ctx) {
        if (enabled) {
            ctx.replaceBean(IBrokerService.class, this.getClass());
        } else {
            //don't record the change so that contexts can be actually reused (since this stub gets removed)
            ctx.deactivateBean(this.getClass(), false);
        }
    }

    @Override
    public GetWorkingProxiesResponse getWorkingProxies() {
        final GetWorkingProxiesResponse response = new GetWorkingProxiesResponse();
        response.getWorkingProxies().addAll(proxies.values());
        return response;
    }

    @Override
    public void addToBeVerifiedProxies(final AddToBeVerifiedProxiesRequest request) {
        rawProxies.addAll(request.getToBeVerifiedProxies());
    }

    @Override
    public GetTaskForCrawlerResponse getTaskForCrawler() {
        final GetTaskForCrawlerResponse response = new GetTaskForCrawlerResponse();
        response.setCrawlForProxies(crawlForProxies);
        if (response.isCrawlForProxies()) {
            BrokerServiceStub.crawlForProxies = false;
        } else {
            for (final RawProxy rawProxy : Iterables.limit(rawProxies, BrokerContractProperties.MAX_PROXIES_PER_TASK)) {
                response.getToBeVerifiedProxies().add(rawProxy);
                rawProxies.remove(rawProxy);
            }
        }
        for (int i = 1; i < 100; i++) {
            response.getToBeScannedPorts().add(i);
        }
        return response;
    }

    @Override
    public void processResultFromCrawler(final ProcessResultFromCrawlerRequest request) {
        addProxies(request.getSuccessfullyVerifiedProxies());
        removeProxies(request.getUnsuccessfullyVerifiedProxies());
    }

    private void addProxies(final List<Proxy> add) {
        for (final Proxy proxy : add) {
            final RawProxy raw = new RawProxy();
            raw.setHost(proxy.getHost());
            raw.setPort(proxy.getPort());
            proxies.put(raw, proxy);
        }
    }

    private void removeProxies(final List<RawProxy> remove) {
        for (final RawProxy rawProxy : remove) {
            proxies.remove(rawProxy);
        }
    }

    public static void setEnabled(final boolean enabled) {
        BrokerServiceStub.enabled = enabled;
    }

    public static boolean isEnabled() {
        return enabled;
    }
}
