package de.invesdwin.webproxy.internal.get.string.httpclient.socketfactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.annotation.concurrent.Immutable;

import org.apache.http.HttpHost;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.protocol.HttpContext;

import de.invesdwin.webproxy.broker.contract.ProxyUtil;
import de.invesdwin.webproxy.broker.contract.schema.Proxy;

@Immutable
public final class PlainSocksProxySocketFactory extends ASocksProxySocketFactory {

    private static final PlainSocksProxySocketFactory INSTANCE = new PlainSocksProxySocketFactory();
    private final PlainConnectionSocketFactory delegate = PlainConnectionSocketFactory.getSocketFactory();

    private PlainSocksProxySocketFactory() {}

    public static PlainSocksProxySocketFactory getInstance() {
        return INSTANCE;
    }

    @Override
    protected Socket createSocket(final HttpContext params, final Proxy proxy) throws IOException {
        if (proxy == null) {
            return delegate.createSocket(params);
        } else {
            return new Socket(ProxyUtil.toJavaProxy(proxy));
        }
    }

    @Override
    public Socket connectSocket(final int connectTimeout, final Socket sock, final HttpHost host,
            final InetSocketAddress remoteAddress, final InetSocketAddress localAddress, final HttpContext context)
            throws IOException {
        //ignore proxy, because we already use it
        return delegate.connectSocket(connectTimeout, sock, host, remoteAddress, localAddress, context);
    }

}
