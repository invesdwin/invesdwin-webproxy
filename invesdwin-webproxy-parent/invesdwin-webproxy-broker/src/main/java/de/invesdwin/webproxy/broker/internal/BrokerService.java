package de.invesdwin.webproxy.broker.internal;

import java.net.InetAddress;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import javax.inject.Named;

import de.invesdwin.context.integration.retry.RetryLaterException;
import de.invesdwin.util.lang.uri.Addresses;
import de.invesdwin.webproxy.broker.contract.IBrokerService;
import de.invesdwin.webproxy.broker.contract.schema.BrokerRequest.AddToBeVerifiedProxiesRequest;
import de.invesdwin.webproxy.broker.contract.schema.BrokerRequest.ProcessResultFromCrawlerRequest;
import de.invesdwin.webproxy.broker.contract.schema.BrokerResponse.GetTaskForCrawlerResponse;
import de.invesdwin.webproxy.broker.contract.schema.BrokerResponse.GetWorkingProxiesResponse;
import de.invesdwin.webproxy.broker.contract.schema.Proxy;
import de.invesdwin.webproxy.broker.contract.schema.RawProxy;
import de.invesdwin.webproxy.broker.internal.persistence.ProxyDao;
import de.invesdwin.webproxy.broker.internal.persistence.ProxyEntity;
import de.invesdwin.webproxy.broker.internal.persistence.RawProxyDao;
import de.invesdwin.webproxy.broker.internal.persistence.RawProxyEntity;

@ThreadSafe
@Named
public class BrokerService implements IBrokerService {

    @Inject
    private ProxyDao proxyDao;
    @Inject
    private RawProxyDao rawProxyDao;
    @Inject
    private BrokerServiceCrawlerTaskHelper crawlerTasksHelper;

    @Override
    public GetWorkingProxiesResponse getWorkingProxies() throws RetryLaterException {
        final GetWorkingProxiesResponse response = new GetWorkingProxiesResponse();
        for (final ProxyEntity ent : proxyDao.findAll()) {
            final Proxy proxy = ent.toProxy();
            response.getWorkingProxies().add(proxy);
        }
        if (response.getWorkingProxies().size() == 0) {
            throw new RetryLaterException(
                    "Currently there are no working proxies, because they have to be crawled initially.");
        }
        return response;
    }

    @Override
    public void addToBeVerifiedProxies(final AddToBeVerifiedProxiesRequest request) {
        for (final RawProxy rawProxy : request.getToBeVerifiedProxies()) {
            final InetAddress addr = Addresses.asAddress(rawProxy.getHost());
            if (!addr.isLoopbackAddress() && !addr.isSiteLocalAddress()) {
                if (proxyDao.shouldRawProxyStillBeVerified(rawProxy)) {
                    rawProxyDao.writeIfNotExists(RawProxyEntity.valueOf(rawProxy));
                }
            }
        }
    }

    @Override
    public GetTaskForCrawlerResponse getTaskForCrawler() {
        return crawlerTasksHelper.getTaskForCrawler();
    }

    @Override
    public void processResultFromCrawler(final ProcessResultFromCrawlerRequest request) {
        for (final Proxy proxy : request.getSuccessfullyVerifiedProxies()) {
            final ProxyEntity ent = ProxyEntity.valueOf(proxy);
            proxyDao.writeOrUpdate(ent);
        }
        for (final RawProxy rawProxy : request.getUnsuccessfullyVerifiedProxies()) {
            proxyDao.deleteIfDowntimeToleranceExceeded(rawProxy);
        }
    }

}