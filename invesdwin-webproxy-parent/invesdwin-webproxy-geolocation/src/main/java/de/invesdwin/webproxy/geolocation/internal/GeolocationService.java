package de.invesdwin.webproxy.geolocation.internal;

import javax.annotation.concurrent.ThreadSafe;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import com.maxmind.geoip.Location;

import de.invesdwin.context.integration.retry.RetryLaterException;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.webproxy.geolocation.contract.IGeolocationService;
import de.invesdwin.webproxy.geolocation.contract.schema.GeolocationRequest.GetGeolocationByCoordinatesRequest;
import de.invesdwin.webproxy.geolocation.contract.schema.GeolocationRequest.GetGeolocationByHostRequest;
import de.invesdwin.webproxy.geolocation.contract.schema.GeolocationResponse.GetGeolocationResponse;
import de.invesdwin.webproxy.geolocation.internal.geoip.IGeoIPService;
import de.invesdwin.webproxy.geolocation.internal.geonames.IGeoNamesService;
import de.invesdwin.webproxy.geolocation.internal.geonames.persistence.AToponymEntity;

@Named
@ThreadSafe
public class GeolocationService implements IGeolocationService {

    @Inject
    private IGeoIPService geoip;
    @Inject
    private IGeoNamesService geonames;

    @Override
    public GetGeolocationResponse getGeolocation(final GetGeolocationByHostRequest request) throws RetryLaterException {
        Assertions.assertThat(request.getHost())
        .as("Host may not be null in %s!", GetGeolocationByHostRequest.class.getSimpleName())
        .isNotNull();
        final Location location = geoip.getLocation(request.getHost());
        //Geonames wird auch für diese Anfragen verwendet, weil die Genauigkeit von GeoIP geringer ist
        final GetGeolocationByCoordinatesRequest requestByKoordinaten = new GetGeolocationByCoordinatesRequest();
        requestByKoordinaten.setLatitude(location.latitude);
        requestByKoordinaten.setLongitude(location.longitude);
        return getGeolocation(requestByKoordinaten);
    }

    @Override
    public GetGeolocationResponse getGeolocation(final GetGeolocationByCoordinatesRequest request)
            throws RetryLaterException {
        final AToponymEntity toponym = geonames.getToponym(request.getLatitude(), request.getLongitude());
        final GetGeolocationResponse response = new GetGeolocationResponse();
        response.mergeFrom(toponym);
        return response;
    }

}
