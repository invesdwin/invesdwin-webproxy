package de.invesdwin.webproxy.internal.get;

import java.net.URI;

import javax.annotation.concurrent.Immutable;

import de.invesdwin.webproxy.callbacks.AProxyResponseCallback;

/**
 * Here a blacklist is being checked to verify that the proxy did not send an invalid result without transmitting the
 * actual content of the requested webpage. To do this, the exception texts are being checked aswell.
 * 
 * This encompasses any failure that may be caused by a proxy.
 * 
 */
@Immutable
final class IsNotProxiesFaultProxyResponseCallback extends AProxyResponseCallback {

    public static final IsNotProxiesFaultProxyResponseCallback INSTANCE = new IsNotProxiesFaultProxyResponseCallback();

    private IsNotProxiesFaultProxyResponseCallback() {}

    /**
     * Ain't the most pretty way to implement this, but this way it is easy to debug.
     */
    @Override
    public boolean isValidResponse(final URI uri, final String response, final Object originalResponse) {
        if (!super.isValidResponse(uri, response, originalResponse)) {
            return false;
        }

        final String lowercase = response.toLowerCase();

        //500 Internal Server Error for ...
        //503 Too many open connections for ...
        boolean proxyFault = lowercase.matches("[0-9]{3}.* for .*");
        //connection to ... refused
        //connection refused
        proxyFault = proxyFault || lowercase.matches("connection.* refused");
        proxyFault = proxyFault || lowercase.contains("connection reset");
        //connect timed out
        //read timed out
        //Connect to /80.191.94.1:3128 timed out
        proxyFault = proxyFault || lowercase.contains("timed out");
        //Malformed reply from SOCKS server
        //Reply from SOCKS server has bad version
        //SOCKS: Host unreachable
        //SOCKS : No acceptable methods
        //SOCKS : authentication failed
        proxyFault = proxyFault || lowercase.contains("socks");
        proxyFault = proxyFault || lowercase.contains("no route to host");
        //Truncated chunk ( expected size: 1425; actual size: 0)
        proxyFault = proxyFault || lowercase.contains("truncated chunk");
        proxyFault = proxyFault || lowercase.contains("the target server failed to respond");
        proxyFault = proxyFault || lowercase.contains("network is unreachable");
        //Too many open connections/Too many open files
        proxyFault = proxyFault || lowercase.contains("too many open");
        proxyFault = proxyFault || lowercase.contains("host not available");
        //Mikrotik HttpProxy
        proxyFault = proxyFault || lowercase.contains("mikrotik");
        //Generated Sun, 19 Jun 2011 18:54:56 GMT by servidorcund (squid/2.7.STABLE5)
        proxyFault = proxyFault || lowercase.contains("squid");
        //nginx/1.0.0
        proxyFault = proxyFault || lowercase.contains("nginx");
        //Error!
        proxyFault = proxyFault || "error!".equals(lowercase);
        //Proxy Error ... Apache/2.2.3 (CentOS) Server at
        proxyFault = proxyFault || lowercase.contains("proxy") && lowercase.contains("apache");
        //Broken Pipe
        proxyFault = proxyFault || lowercase.contains("broken pipe");
        //403 Forbidden
        proxyFault = proxyFault || lowercase.contains("forbidden");
        //502 Bad Gateway
        proxyFault = proxyFault || lowercase.contains("bad gateway");
        //alt="INTERSAFE"
        proxyFault = proxyFault || lowercase.contains("intersafe");
        //The requested URL http://ichart.finance.yahoo.com/table.csv?s=3543.TWO&amp;g=d was not found on this server.
        proxyFault = proxyFault || lowercase.matches("the requested url .* was not found on this server\\.");
        //Not Found or 404 should not be checked here, because yahoo downloads might accept this as a valid response
        //Error 404: Not Found
        proxyFault = proxyFault || lowercase.contains("error 404");
        //Error 503 Service unavailable
        proxyFault = proxyFault || lowercase.contains("error 503");

        return !proxyFault;
    }

}
