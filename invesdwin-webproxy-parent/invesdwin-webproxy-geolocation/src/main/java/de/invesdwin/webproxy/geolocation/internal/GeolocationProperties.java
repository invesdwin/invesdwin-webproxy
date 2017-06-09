package de.invesdwin.webproxy.geolocation.internal;

import java.net.URL;

import javax.annotation.concurrent.Immutable;

import de.invesdwin.context.system.properties.SystemProperties;
import de.invesdwin.util.lang.uri.URIs;

@Immutable
public final class GeolocationProperties {

    public static final URL GEOIP_DATA_URL;
    public static final URL GEONAMES_DATA_URL;
    public static final int CLASSIFICATIONS_PER_AXIS = 90;

    private static final SystemProperties SYSTEM_PROPERTIES = new SystemProperties(GeolocationProperties.class);

    private GeolocationProperties() {}

    static {
        GEOIP_DATA_URL = URIs.asUrl(SYSTEM_PROPERTIES.getString("GEOIP_DATA_URL"));
        GEONAMES_DATA_URL = URIs.asUrl(SYSTEM_PROPERTIES.getString("GEONAMES_DATA_URL"));
    }

}
