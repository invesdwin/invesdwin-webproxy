package de.invesdwin.webproxy.broker.contract.internal;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.concurrent.ThreadSafe;

import de.invesdwin.context.test.stub.StubSupport;
import de.invesdwin.util.collections.Collections;
import de.invesdwin.util.collections.Iterables;
import de.invesdwin.webproxy.broker.contract.BrokerContractProperties;
import de.invesdwin.webproxy.broker.contract.BrokerServiceStub;
import de.invesdwin.webproxy.broker.contract.IBrokerService;
import de.invesdwin.webproxy.broker.contract.schema.BrokerRequest.AddToBeVerifiedProxiesRequest;
import de.invesdwin.webproxy.broker.contract.schema.BrokerRequest.ProcessResultFromCrawlerRequest;
import de.invesdwin.webproxy.broker.contract.schema.BrokerResponse.GetTaskForCrawlerResponse;
import de.invesdwin.webproxy.broker.contract.schema.BrokerResponse.GetWorkingProxiesResponse;
import de.invesdwin.webproxy.broker.contract.schema.Proxy;
import de.invesdwin.webproxy.broker.contract.schema.RawProxy;
import jakarta.inject.Named;

@Named
@ThreadSafe
public class InMemoryBrokerServiceStub extends StubSupport implements IBrokerService {

    private final Map<RawProxy, Proxy> proxies = new ConcurrentHashMap<RawProxy, Proxy>();
    private final Set<RawProxy> rawProxies = Collections.newSetFromMap(new ConcurrentHashMap<RawProxy, Boolean>());

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
        response.setCrawlForProxies(BrokerServiceStub.isCrawlForProxies());
        if (response.isCrawlForProxies()) {
            BrokerServiceStub.setCrawlForProxies(false);
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

}
