package de.invesdwin.webproxy.internal.get.string.httpclient.socketfactory;

import java.io.IOException;
import java.net.Socket;

import javax.annotation.concurrent.Immutable;

import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.protocol.HttpContext;

import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.webproxy.broker.contract.schema.Proxy;
import de.invesdwin.webproxy.broker.contract.schema.ProxyType;

@Immutable
public abstract class ASocksProxySocketFactory implements ConnectionSocketFactory {

    public static final String HTTP_PARAM_SOCKS_PROXY = "HTTP_PARAM_SOCKS_PROXY";

    @Override
    public Socket createSocket(final HttpContext context) throws IOException {
        final Proxy proxy = (Proxy) context.getAttribute("HTTP_PARAM_SOCKS_PROXY");
        if (proxy != null) {
            Assertions.assertThat(proxy.getType()).isSameAs(ProxyType.SOCKS);
        }
        return createSocket(context, proxy);
    }

    /**
     * proxy may also be null if no socks proxy is needed for this socket.
     */
    protected abstract Socket createSocket(HttpContext context, Proxy proxy) throws IOException;

}
