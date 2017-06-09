package de.invesdwin.webproxy.internal.get.string.httpclient.socketfactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.annotation.concurrent.Immutable;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpHost;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.protocol.HttpContext;

import de.invesdwin.context.log.error.Err;
import de.invesdwin.webproxy.broker.contract.schema.Proxy;

@Immutable
public final class SslSocksProxySocketFactory extends ASocksProxySocketFactory implements
        LayeredConnectionSocketFactory {

    private static final SslSocksProxySocketFactory INSTANCE = new SslSocksProxySocketFactory();

    private final PlainSocksProxySocketFactory proxyLayerDelegate = PlainSocksProxySocketFactory.getInstance();
    private final SSLConnectionSocketFactory delegate = initSocketFactory();

    private SslSocksProxySocketFactory() {}

    public static SslSocksProxySocketFactory getInstance() {
        return INSTANCE;
    }

    @Override
    protected Socket createSocket(final HttpContext params, final Proxy proxy) throws IOException {
        return proxyLayerDelegate.createSocket(params, proxy);
    }

    /**
     * Workaround for HTTPS over SOCKS proxy. The SSL certificate mechanism is disabled by this. Since we just want to
     * crawl some data with this implementation, this is of no concern. To really support secure SSL connection either
     * this webproxy module needs to be made configurable for this, or the connection has to be established by some
     * other tool. For now secure SSL connections are not needed in webproxy in my opinion. Logins shouldn't be done
     * with the webproxy module anyway. :)
     * 
     * You can get the normal SocketFactory by calling: SSLSocketFactory.getSocketFactory();
     * 
     * That one throws on self signed certificates: javax.net.ssl.SSLPeerUnverifiedException: peer not authenticated
     * 
     * @see <a href="http://forums.sun.com/thread.jspa?messageID=10727992#10727992">Source</a>
     */
    private static SSLConnectionSocketFactory initSocketFactory() {
        final SSLContext sslcontext = createTrustAnythingSslContext();
        // Use the above SSLContext to create your socket factory
        // (I found trying to extend the factory a bit difficult due to a
        // call to createSocket with no arguments, a method which doesn't
        // exist anywhere I can find, but hey-ho).
        return new SSLConnectionSocketFactory(sslcontext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

    }

    public static SSLContext createTrustAnythingSslContext() {
        try {
            // First create a trust manager that won't care.
            final X509TrustManager trustManager = new X509TrustManager() {
                @Override
                public void checkClientTrusted(final X509Certificate[] chain, final String authType) {
                    // Don't do anything.
                }

                @Override
                public void checkServerTrusted(final X509Certificate[] chain, final String authType) {
                    // Don't do anything.
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    // Don't do anything.
                    return null;
                }
            };

            // Now put the trust manager into an SSLContext.
            final SSLContext sslcontext = SSLContext.getInstance("TLS");
            sslcontext.init(null, new TrustManager[] { trustManager }, null);
            return sslcontext;
        } catch (final NoSuchAlgorithmException e) {
            throw Err.process(e);
        } catch (final KeyManagementException e) {
            throw Err.process(e);
        }
    }

    @Override
    public Socket connectSocket(final int connectTimeout, final Socket sock, final HttpHost host,
            final InetSocketAddress remoteAddress, final InetSocketAddress localAddress, final HttpContext context)
            throws IOException {
        //First we connect directly with the socket
        final Socket socket = proxyLayerDelegate.connectSocket(connectTimeout, sock, host, remoteAddress, localAddress,
                context);
        //Then we create the ssl session inside of it
        return delegate.createLayeredSocket(socket, remoteAddress.getAddress().getHostAddress(),
                remoteAddress.getPort(), context);
    }

    /**
     * LayeredSocketFactory must be implemented, so that ssl works inside a socks proxy.
     * 
     * @see <a href="http://www.mail-archive.com/httpclient-users@hc.apache.org/msg01506.html">Source</a>
     */
    @Override
    public Socket createLayeredSocket(final Socket socket, final String target, final int port,
            final HttpContext context) throws IOException {
        return delegate.createLayeredSocket(socket, target, port, context);
    }

}
