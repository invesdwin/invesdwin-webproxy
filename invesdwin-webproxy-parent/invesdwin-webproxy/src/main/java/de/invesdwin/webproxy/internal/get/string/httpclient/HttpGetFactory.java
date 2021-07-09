package de.invesdwin.webproxy.internal.get.string.httpclient;

import java.net.URI;

import javax.annotation.concurrent.ThreadSafe;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.protocol.HttpContext;

import com.gargoylesoftware.htmlunit.BrowserVersion;

import de.invesdwin.context.ContextProperties;
import de.invesdwin.util.error.UnknownArgumentException;
import de.invesdwin.util.time.date.FTimeUnit;
import de.invesdwin.webproxy.broker.contract.schema.Proxy;
import de.invesdwin.webproxy.broker.contract.schema.ProxyType;
import de.invesdwin.webproxy.internal.get.string.httpclient.socketfactory.ASocksProxySocketFactory;

@ThreadSafe
public final class HttpGetFactory {

    public static final HttpClient SHARED_CLIENT;

    private HttpGetFactory() {}

    static {
        SHARED_CLIENT = HttpClientFactory.newInstance();
    }

    /************************** public methods *********************/

    public static HttpGet newHttpGet(final URI uri, final Proxy proxy, final BrowserVersion browserVersion) {
        final HttpGet httpget = new HttpGet(uri);
        final RequestConfig.Builder config = RequestConfig.custom();
        final int timeout = ContextProperties.DEFAULT_NETWORK_TIMEOUT.intValue(FTimeUnit.MILLISECONDS);
        config.setConnectTimeout(timeout);
        config.setSocketTimeout(timeout);
        config.setStaleConnectionCheckEnabled(false);
        if (proxy != null) {
            final ProxyType typ = proxy.getType();
            switch (typ) {
            case HTTP:
                final HttpHost proxyHost = new HttpHost(proxy.getHost(), proxy.getPort());
                config.setProxy(proxyHost);
                break;
            case SOCKS:
                //see newHttpContext
                break;
            default:
                throw UnknownArgumentException.newInstance(ProxyType.class, typ);
            }
        }
        httpget.setConfig(config.build());
        httpget.setHeader(HttpHeaders.USER_AGENT, browserVersion.getUserAgent());
        return httpget;
    }

    public static HttpContext newHttpContext(final Proxy proxy) {
        final HttpContext context = new HttpClientContext();
        if (proxy != null) {
            final ProxyType typ = proxy.getType();
            switch (typ) {
            case HTTP:
                //see newHttpGet
                break;
            case SOCKS:
                context.setAttribute(ASocksProxySocketFactory.HTTP_PARAM_SOCKS_PROXY, proxy);
                break;
            default:
                throw UnknownArgumentException.newInstance(ProxyType.class, typ);
            }
        }
        return context;
    }

}
