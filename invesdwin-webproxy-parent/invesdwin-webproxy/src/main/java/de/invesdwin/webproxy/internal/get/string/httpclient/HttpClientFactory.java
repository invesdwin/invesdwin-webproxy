package de.invesdwin.webproxy.internal.get.string.httpclient;

import javax.annotation.concurrent.Immutable;

import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import de.invesdwin.webproxy.WebproxyProperties;
import de.invesdwin.webproxy.internal.get.string.httpclient.socketfactory.PlainSocksProxySocketFactory;
import de.invesdwin.webproxy.internal.get.string.httpclient.socketfactory.SslSocksProxySocketFactory;

// CHECKSTYLE:OFF
@Immutable
public final class HttpClientFactory {
    //CHECKSTYLE:ON

    private HttpClientFactory() {}

    public static CloseableHttpClient newInstance() {
        final Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory> create()
                .register("http", PlainSocksProxySocketFactory.getInstance())
                .register("https", SslSocksProxySocketFactory.getInstance())
                .build();
        final PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(registry);
        connManager.setMaxTotal(WebproxyProperties.MAX_PARALLEL_DOWNLOADS);
        connManager.setDefaultMaxPerRoute(connManager.getMaxTotal());
        final HttpClientBuilder httpclient = HttpClientBuilder.create();
        httpclient.setConnectionManager(connManager);
        httpclient.disableCookieManagement();
        return httpclient.build();
    }

}
