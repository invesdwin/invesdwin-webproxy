package de.invesdwin.webproxy.broker.contract;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Named;

import com.google.common.collect.Iterables;

import de.invesdwin.context.beans.init.locations.PositionedResource;
import de.invesdwin.context.test.ATest;
import de.invesdwin.context.test.TestContext;
import de.invesdwin.context.test.stub.StubSupport;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.webproxy.broker.contract.internal.BrokerContextLocation;
import de.invesdwin.webproxy.broker.contract.schema.BrokerRequest.AddToBeVerifiedProxiesRequest;
import de.invesdwin.webproxy.broker.contract.schema.BrokerRequest.ProcessResultFromCrawlerRequest;
import de.invesdwin.webproxy.broker.contract.schema.BrokerResponse.GetTaskForCrawlerResponse;
import de.invesdwin.webproxy.broker.contract.schema.BrokerResponse.GetWorkingProxiesResponse;
import de.invesdwin.webproxy.broker.contract.schema.Proxy;
import de.invesdwin.webproxy.broker.contract.schema.RawProxy;

@Named
@ThreadSafe
public class BrokerServiceStub extends StubSupport implements IBrokerService {

    private static boolean enabled = true;

    private static volatile boolean crawlForProxies;

    private final Set<Proxy> proxies = Collections.newSetFromMap(new ConcurrentHashMap<Proxy, Boolean>());
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
    public void setUpContext(final ATest test, final TestContext ctx) {
        if (enabled) {
            ctx.replaceBean(IBrokerService.class, this.getClass());
        } else {
            ctx.deactivateBean(this.getClass());
        }
    }

    @Override
    public GetWorkingProxiesResponse getWorkingProxies() {
        final GetWorkingProxiesResponse response = new GetWorkingProxiesResponse();
        response.getWorkingProxies().addAll(proxies);
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
        proxies.addAll(request.getSuccessfullyVerifiedProxies());
        proxies.removeAll(request.getUnsuccessfullyVerifiedProxies());
    }

    public static void setEnabled(final boolean enabled) {
        BrokerServiceStub.enabled = enabled;
    }

    public static boolean isEnabled() {
        return enabled;
    }
}
