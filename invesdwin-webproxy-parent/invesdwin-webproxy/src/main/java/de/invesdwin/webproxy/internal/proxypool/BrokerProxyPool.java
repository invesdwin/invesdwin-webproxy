package de.invesdwin.webproxy.internal.proxypool;

import java.io.IOException;

import javax.annotation.concurrent.ThreadSafe;
import jakarta.inject.Inject;

import org.springframework.beans.factory.annotation.Configurable;

import de.invesdwin.context.log.error.Err;
import de.invesdwin.util.concurrent.Threads;
import de.invesdwin.webproxy.ProxyVerification;
import de.invesdwin.webproxy.broker.contract.schema.ProxyQuality;
import de.invesdwin.webproxy.callbacks.statistics.basis.AStatisticsCallback;
import de.invesdwin.webproxy.callbacks.statistics.basis.ProxyStatistics;
import de.invesdwin.webproxy.callbacks.statistics.basis.SessionStatistics;
import de.invesdwin.webproxy.internal.get.AGetConfig;

/**
 * Manages a specific number of proxies for reuse. It grows dynamically and shrinks by discarded proxies on finished
 * downloads.
 * 
 * For expired proxies there are no ProxyStatistics.
 * 
 * @author subes
 * 
 */
@ThreadSafe
@Configurable
public class BrokerProxyPool implements IProxyPool {

    //Proxy cooldown is also being enforced by proxy rotation
    @Inject
    private BrokerProxyObjectPool brokerProxyObjectPool;
    @Inject
    private ProxyVerification proxyVeri;

    @Override
    public PooledProxy getProxy(final AGetConfig config, final SessionStatistics session) throws InterruptedException {
        try {
            while (true) {
                Threads.throwIfInterrupted();
                //If another one has been freed now, we use that one
                final PooledProxy freeProxy = brokerProxyObjectPool.borrowObject();
                if (!proxyVeri.isOfMinProxyQuality(freeProxy, config.getMinProxyQuality())) {
                    /*
                     * So that we even get a change to get a proxy in the requested quality, we discard inproper proxies
                     * directly to make the proxy rotation faster.
                     */
                    discardProxy(config, session, freeProxy, new IOException(ProxyQuality.class.getSimpleName()
                            + " is not at least " + config.getMinProxyQuality() + " but " + freeProxy.getQuality()));
                } else {
                    /*
                     * The proxy matches the requirements and is ready for downloads.
                     */
                    freeProxy.downloadTry();
                    return freeProxy;
                }
            }
        } catch (final InterruptedException e) {
            throw e;
        } catch (final Exception e) {
            throw Err.process(e);
        }
    }

    @Override
    public void discardProxy(final AGetConfig config, final SessionStatistics session, final PooledProxy proxy,
            final Throwable reason) {
        try {
            brokerProxyObjectPool.invalidateObject(proxy);
        } catch (final Exception e) {
            throw Err.process(e);
        }
        final AStatisticsCallback callback = config.getStatisticsCallback();
        if (callback != null) {
            final ProxyStatistics statistics = proxy.toStatistics(brokerProxyObjectPool.size(), reason);
            callback.proxyNotWorkingAnymore(session, statistics);
        }
    }

    @Override
    public void returnProxy(final AGetConfig config, final SessionStatistics session, final PooledProxy proxy,
            final boolean downloadSuccessful) {
        if (downloadSuccessful) {
            proxy.downloadTrySuccessful();
            final AStatisticsCallback callback = config.getStatisticsCallback();
            if (callback != null) {
                final ProxyStatistics statistics = proxy.toStatistics(brokerProxyObjectPool.size(), null);
                callback.proxyStillWorks(session, statistics);
            }
        }
        try {
            brokerProxyObjectPool.returnObject(proxy);
        } catch (final Exception e) {
            throw Err.process(e);
        }
    }

}
