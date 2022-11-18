package de.invesdwin.webproxy;

import javax.annotation.concurrent.Immutable;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.HttpClientBuilder;

import com.gargoylesoftware.htmlunit.HttpWebConnection;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.ProxyConfig;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.javascript.background.BackgroundJavaScriptFactory;

import de.invesdwin.context.ContextProperties;
import de.invesdwin.util.time.date.FTimeUnit;
import de.invesdwin.webproxy.broker.contract.schema.Proxy;
import de.invesdwin.webproxy.broker.contract.schema.ProxyType;
import de.invesdwin.webproxy.internal.get.page.htmlunit.CountingRefreshHandler;
import de.invesdwin.webproxy.internal.get.page.htmlunit.js.HackedBackgroundJavaScriptFactory;
import de.invesdwin.webproxy.internal.get.string.httpclient.socketfactory.SslSocksProxySocketFactory;

/**
 * For socks proxies to work properly, the webproxy SocketFactories are used here.
 */
@Immutable
public final class WebClientFactory {

    static {
        BackgroundJavaScriptFactory.setFactory(new HackedBackgroundJavaScriptFactory());
    }

    private WebClientFactory() {}

    /**
     * Webclients are not thread safe, thus each download needs to have its own.
     */
    public static WebClient initWebClient(final GetPageConfig config, final Proxy proxy) {
        final WebClient client = new WebClient(config.getBrowserVersion());
        client.getOptions().setThrowExceptionOnFailingStatusCode(false);
        client.getOptions().setThrowExceptionOnScriptError(false);
        client.getOptions().setJavaScriptEnabled(config.isJavascriptEnabled());
        client.setAjaxController(new NicelyResynchronizingAjaxController());
        client.setWebConnection(new HttpWebConnection(client) {
            @Override
            protected HttpClientBuilder createHttpClientBuilder() {
                final HttpClientBuilder builder = super.createHttpClientBuilder();
                builder.setSSLHostnameVerifier(new NoopHostnameVerifier());
                builder.setMaxConnTotal(WebproxyProperties.MAX_PARALLEL_DOWNLOADS);
                builder.setMaxConnPerRoute(WebproxyProperties.MAX_PARALLEL_DOWNLOADS);
                builder.setSSLContext(SslSocksProxySocketFactory.createTrustAnythingSslContext());
                return builder;
            }
        });
        //Javascripttimeout is always needed to prevent deadlocks!
        if (config.getJavascriptWaitCallback() != null) {
            client.setJavaScriptTimeout(
                    config.getJavascriptWaitCallback().getMaxDelay().longValue(FTimeUnit.MILLISECONDS));
        } else {
            client.setJavaScriptTimeout(ContextProperties.DEFAULT_NETWORK_TIMEOUT.intValue(FTimeUnit.MILLISECONDS));
        }
        client.getOptions().setCssEnabled(config.isCssEnabled());
        client.getOptions().setTimeout(ContextProperties.DEFAULT_NETWORK_TIMEOUT.intValue(FTimeUnit.MILLISECONDS));
        if (proxy != null) {
            client.getOptions()
                    .setProxyConfig(new ProxyConfig(proxy.getHost(), proxy.getPort(), "http",
                            proxy.getType() == ProxyType.SOCKS));
        } else {
            client.getOptions().setProxyConfig(new ProxyConfig());
        }
        //always insecure ssl to allow self signed certificates
        client.getOptions().setUseInsecureSSL(true);
        client.setRefreshHandler(new CountingRefreshHandler(config));
        return client;
    }
}
