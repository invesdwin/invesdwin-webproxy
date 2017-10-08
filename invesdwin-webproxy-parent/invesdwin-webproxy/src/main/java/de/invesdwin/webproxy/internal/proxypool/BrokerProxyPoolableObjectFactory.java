package de.invesdwin.webproxy.internal.proxypool;

import java.util.ArrayDeque;
import java.util.Queue;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import javax.inject.Named;

import de.invesdwin.context.integration.retry.Retry;
import de.invesdwin.context.integration.retry.RetryLaterException;
import de.invesdwin.context.integration.retry.RetryLaterRuntimeException;
import de.invesdwin.context.pool.IPoolableObjectFactory;
import de.invesdwin.webproxy.ProxyVerification;
import de.invesdwin.webproxy.WebproxyProperties;
import de.invesdwin.webproxy.broker.contract.IBrokerService;
import de.invesdwin.webproxy.broker.contract.schema.BrokerResponse.GetWorkingProxiesResponse;
import de.invesdwin.webproxy.broker.contract.schema.Proxy;
import de.invesdwin.webproxy.broker.contract.schema.ProxyQuality;

@ThreadSafe
@Named
public final class BrokerProxyPoolableObjectFactory implements IPoolableObjectFactory<PooledProxy> {

    @GuardedBy("PROXY_CACHE")
    private static final Queue<Proxy> PROXY_CACHE = new ArrayDeque<Proxy>();

    @Inject
    private IBrokerService broker;
    @Inject
    private ProxyVerification proxyVeri;

    private Integer lastWorkingProxyCount;

    private BrokerProxyPoolableObjectFactory() {}

    @Override
    public PooledProxy makeObject() {
        synchronized (PROXY_CACHE) {
            if (PROXY_CACHE.isEmpty()) {
                tryGetWorkingProxies(true);

            }
            return new PooledProxy(PROXY_CACHE.poll());
        }
    }

    public int getLastWorkingProxyCount() {
        if (lastWorkingProxyCount == null) {
            tryGetWorkingProxies(false);
        }
        return lastWorkingProxyCount;
    }

    @Retry
    private void tryGetWorkingProxies(final boolean retry) {
        try {
            final GetWorkingProxiesResponse response = broker.getWorkingProxies();
            lastWorkingProxyCount = response.getWorkingProxies().size();
            PROXY_CACHE.addAll(response.getWorkingProxies());
        } catch (final RetryLaterException e) {
            if (retry) {
                throw new RetryLaterRuntimeException(e);
            } else {
                lastWorkingProxyCount = 0;
                return;
            }
        }
    }

    @Override
    public boolean validateObject(final PooledProxy proxy) {
        if (proxy.isWarmupTimeoutExpired()) {
            try {
                final boolean notifyNotWorking = !proxy.isWarmedUp()
                        && WebproxyProperties.AUTO_NOTIFY_ABOUT_NOT_WORKING_POOLED_PROXIES;
                return proxyVeri.verifyProxy(proxy, notifyNotWorking, ProxyQuality.TRANSPARENT);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        } else {
            return proxyVeri.isOfMinProxyQuality(proxy, ProxyQuality.TRANSPARENT);
        }
    }

    @Override
    public void destroyObject(final PooledProxy obj) throws Exception {}

    @Override
    public void activateObject(final PooledProxy obj) throws Exception {}

    @Override
    public void passivateObject(final PooledProxy obj) throws Exception {}

}
