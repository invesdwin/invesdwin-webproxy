package de.invesdwin.webproxy.broker.contract;

import java.net.URI;

import javax.annotation.concurrent.ThreadSafe;

import org.springframework.ws.client.support.destination.DestinationProvider;

import de.invesdwin.context.integration.network.NetworkUtil;
import de.invesdwin.util.lang.uri.URIs;

@ThreadSafe
public final class CheckClient {

    public static final String CHECK = "check";
    public static final String CLIENT_IP = "clientip";
    public static final String CLIENT_IP_PARAM = "{" + CLIENT_IP + "}";
    public static final String PROXY_QUALITY = "proxyquality+" + CLIENT_IP_PARAM + "+check";

    private DestinationProvider destinationProvider;

    public void setDestinationProvider(final DestinationProvider destinationProvider) {
        this.destinationProvider = destinationProvider;
    }

    private URI getBrokerBaseUri() {
        return URIs.asUri(URIs.getBasis(destinationProvider.getDestination()) + "/spring-web");
    }

    public URI getCheckProxyQualityUri() {
        return URIs.asUri(getBrokerBaseUri() + "/" + CHECK + "/"
                + PROXY_QUALITY.replace(CLIENT_IP_PARAM, NetworkUtil.getExternalAddress().getHostAddress()));
    }

    public URI getCheckClientIpUri() {
        return URIs.asUri(getBrokerBaseUri() + "/" + CHECK + "/" + CLIENT_IP);
    }
}
