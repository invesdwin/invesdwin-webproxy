package de.invesdwin.webproxy.internal;

import javax.annotation.concurrent.ThreadSafe;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import com.gargoylesoftware.htmlunit.WebClient;

import de.invesdwin.webproxy.GetPageConfig;
import de.invesdwin.webproxy.GetStringConfig;
import de.invesdwin.webproxy.ProxyVerification;
import de.invesdwin.webproxy.WebClientFactory;
import de.invesdwin.webproxy.broker.contract.schema.Proxy;
import de.invesdwin.webproxy.internal.proxypool.BrokerProxyPoolableObjectFactory;
import de.invesdwin.webproxy.internal.proxypool.PooledProxy;

@Named
@ThreadSafe
public class WebproxyServiceHelper {

    @Inject
    private ProxyVerification proxyVeri;
    @Inject
    private BrokerProxyPoolableObjectFactory brokerProxyPoolableObjectFactory;

    public Proxy newProxy(final GetStringConfig config) throws InterruptedException {
        while (true) {
            final PooledProxy proxy = brokerProxyPoolableObjectFactory.makeObject();
            if (brokerProxyPoolableObjectFactory.validateObject(proxy)
                    && proxyVeri.isOfMinProxyQuality(proxy, config.getMinProxyQuality())) {
                return proxy;
            } else {
                continue;
            }
        }
    }

    public int getLastWorkingProxyCount() {
        return brokerProxyPoolableObjectFactory.getLastWorkingProxyCount();
    }

    public WebClient newWebClient(final GetPageConfig config) {
        Proxy proxy = config.getFixedProxy();
        if (proxy == null && config.isUseProxyPool()) {
            try {
                proxy = newProxy(config);
            } catch (final InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return WebClientFactory.initWebClient(config, proxy);
    }

}
