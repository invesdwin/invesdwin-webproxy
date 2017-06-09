package de.invesdwin.webproxy.broker.contract;

import java.net.InetSocketAddress;

import javax.annotation.concurrent.Immutable;

import de.invesdwin.util.error.UnknownArgumentException;
import de.invesdwin.webproxy.broker.contract.schema.Proxy;
import de.invesdwin.webproxy.broker.contract.schema.ProxyQuality;
import de.invesdwin.webproxy.broker.contract.schema.ProxyType;
import de.invesdwin.webproxy.broker.contract.schema.RawProxy;

@Immutable
public final class ProxyUtil {

    private ProxyUtil() {}

    public static java.net.Proxy toJavaProxy(final Proxy proxy) {
        final InetSocketAddress addr = InetSocketAddress.createUnresolved(proxy.getHost(), proxy.getPort());
        return new java.net.Proxy(java.net.Proxy.Type.valueOf(proxy.getType().toString()), addr);
    }

    public static RawProxy toRawProxy(final Proxy proxy) {
        final RawProxy rawProxy = new RawProxy();
        rawProxy.setHost(proxy.getHost());
        rawProxy.setPort(proxy.getPort());
        return rawProxy;
    }

    public static Proxy valueOf(final String host, final Integer port, final ProxyType type,
            final ProxyQuality quality, final String countryCode, final String timeZoneId) {
        final Proxy proxy = new Proxy();
        proxy.setHost(host);
        proxy.setPort(port);
        proxy.setType(type);
        proxy.setQuality(quality);
        proxy.setCountryCode(countryCode);
        proxy.setTimeZoneId(timeZoneId);
        return proxy;
    }

    public static Proxy valueOf(final RawProxy rawProxy, final ProxyType type, final ProxyQuality quality,
            final String countryCode, final String timeZoneId) {
        return valueOf(rawProxy.getHost(), rawProxy.getPort(), type, quality, timeZoneId, timeZoneId);
    }

    public static RawProxy valueOf(final String host, final Integer port) {
        final RawProxy rawProxy = new RawProxy();
        rawProxy.setHost(host);
        rawProxy.setPort(port);
        return rawProxy;
    }

    public static ProxyQuality valueOf(final String quality) {
        if (quality == null) {
            return null;
        }
        try {
            return ProxyQuality.valueOf(quality);
        } catch (final IllegalArgumentException e) {
            return null;
        }
    }

    public static Proxy valueOf(final java.net.Proxy javaProxy) {
        final Proxy proxy = new Proxy();
        final InetSocketAddress addr = (InetSocketAddress) javaProxy.address();
        proxy.setHost(addr.getHostName());
        proxy.setPort(addr.getPort());
        switch (javaProxy.type()) {
        case HTTP:
            proxy.setType(ProxyType.HTTP);
            break;
        case SOCKS:
            proxy.setType(ProxyType.SOCKS);
            break;
        default:
            throw UnknownArgumentException.newInstance(java.net.Proxy.Type.class, javaProxy.type());
        }
        //unknown here, thus assuming worst
        proxy.setQuality(ProxyQuality.TRANSPARENT);
        return proxy;
    }
}
