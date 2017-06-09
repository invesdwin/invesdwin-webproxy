package de.invesdwin.webproxy.internal.proxypool;

import java.io.IOException;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Configurable;

import de.invesdwin.webproxy.ProxyVerification;
import de.invesdwin.webproxy.broker.contract.schema.Proxy;
import de.invesdwin.webproxy.broker.contract.schema.ProxyQuality;
import de.invesdwin.webproxy.callbacks.statistics.basis.AStatisticsCallback;
import de.invesdwin.webproxy.callbacks.statistics.basis.SessionStatistics;
import de.invesdwin.webproxy.internal.get.AGetConfig;

@ThreadSafe
@Configurable
public class FixedProxyPool implements IProxyPool {

    @GuardedBy("this")
    private final PooledProxy proxy;
    private final boolean proxyVerification;

    @Inject
    private ProxyVerification proxyVeri;

    public FixedProxyPool(final Proxy proxy, final boolean proxyVerification) {
        if (proxy instanceof PooledProxy) {
            this.proxy = (PooledProxy) proxy;
        } else {
            this.proxy = new PooledProxy(proxy);
        }
        this.proxyVerification = proxyVerification;
    }

    @Override
    public synchronized PooledProxy getProxy(final AGetConfig config, final SessionStatistics session)
            throws IOException, InterruptedException {
        //On verification a warmup is not needed, because it would be done twice otherwise
        if (!proxyVerification && proxy.isWarmupTimeoutExpired()) {
            if (!proxyVeri.verifyProxy(proxy, false, config.getMinProxyQuality())) {
                if (proxy.getQuality() == null) {
                    throw new IOException("Proxy does not work");
                } else {
                    throw new IOException("Proxy is not of min " + ProxyQuality.class.getSimpleName() + " ["
                            + config.getMinProxyQuality() + "]");
                }
            }
        }
        proxy.downloadTry();
        return proxy;
    }

    @Override
    public synchronized void discardProxy(final AGetConfig config, final SessionStatistics session,
            final PooledProxy proxy, final Throwable reason) {
        final AStatisticsCallback callback = config.getStatisticsCallback();
        if (callback != null) {
            callback.proxyNotWorkingAnymore(session, proxy.toStatistics(1, reason));
        }
    }

    @Override
    public synchronized void returnProxy(final AGetConfig config, final SessionStatistics session,
            final PooledProxy proxy, final boolean downloadSuccessful) {
        if (downloadSuccessful) {
            proxy.downloadTrySuccessful();
            final AStatisticsCallback callback = config.getStatisticsCallback();
            if (callback != null) {
                callback.proxyStillWorks(session, proxy.toStatistics(1, null));
            }
        }
    }

}
