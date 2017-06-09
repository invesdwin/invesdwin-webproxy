package de.invesdwin.webproxy.geolocation.internal.geoip;

import com.maxmind.geoip.Location;

import de.invesdwin.context.integration.retry.RetryLaterException;

public interface IGeoIPService {

    Location getLocation(String ip) throws RetryLaterException;

}
