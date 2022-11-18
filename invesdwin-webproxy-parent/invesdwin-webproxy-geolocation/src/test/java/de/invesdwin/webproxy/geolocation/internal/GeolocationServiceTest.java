package de.invesdwin.webproxy.geolocation.internal;

import java.util.Locale;

import javax.annotation.concurrent.ThreadSafe;
import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import de.invesdwin.context.integration.retry.RetryLaterException;
import de.invesdwin.context.persistence.jpa.test.APersistenceTest;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.assertions.Executable;
import de.invesdwin.util.time.date.timezone.TimeZones;
import de.invesdwin.webproxy.geolocation.contract.IGeolocationService;
import de.invesdwin.webproxy.geolocation.contract.schema.GeolocationRequest.GetGeolocationByCoordinatesRequest;
import de.invesdwin.webproxy.geolocation.contract.schema.GeolocationRequest.GetGeolocationByHostRequest;
import de.invesdwin.webproxy.geolocation.contract.schema.GeolocationResponse.GetGeolocationResponse;
import de.invesdwin.webproxy.geolocation.internal.geonames.persistence.redundance.ToponymRepository;

@ThreadSafe
public class GeolocationServiceTest extends APersistenceTest {

    @Inject
    private IGeolocationService geolocation;
    @Inject
    private ToponymRepository toponymRepo;

    @Override
    public void setUpOnce() throws Exception {
        super.setUpOnce();
        log.info("There are %s rows in the %s table", toponymRepo.getActiveDao().count(),
                toponymRepo.newActiveToponymEntity().getClass().getSimpleName());
    }

    @Test
    public void testGetGeolocationByHostWithEmptyHost() throws RetryLaterException {
        Assertions.assertThrows(NullPointerException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                final GetGeolocationByHostRequest request = new GetGeolocationByHostRequest();
                geolocation.getGeolocation(request);
            }
        });
    }

    @Test
    public void testGetGeolocationByHostRequest() throws RetryLaterException {
        final GetGeolocationByHostRequest request = new GetGeolocationByHostRequest();
        request.setHost("berlin.de");
        final GetGeolocationResponse response = geolocation.getGeolocation(request);
        Assertions.assertThat(response.getCountryCode()).isEqualTo(Locale.GERMANY.getCountry());
        Assertions.assertThat(response.getTimeZoneId()).isEqualTo("Europe/Berlin");
        Assertions.assertThat(response.getLocationName()).isEqualTo("Berlin");
        Assertions.assertThat(response.getLongitude()).isNotNull();
        Assertions.assertThat(response.getLatitude()).isNotNull();
    }

    @Test
    public void testGetGeolocationByCoordinatesRequest() throws RetryLaterException {
        final GetGeolocationByHostRequest requestByHost = new GetGeolocationByHostRequest();
        requestByHost.setHost("berlin.de");
        final GetGeolocationResponse responseByHost = geolocation.getGeolocation(requestByHost);

        final GetGeolocationByCoordinatesRequest request = new GetGeolocationByCoordinatesRequest();
        request.setLongitude(responseByHost.getLongitude());
        request.setLatitude(responseByHost.getLatitude());
        final GetGeolocationResponse response = geolocation.getGeolocation(request);
        Assertions.assertThat(response.getCountryCode()).isEqualTo(Locale.GERMANY.getCountry());
        Assertions.assertThat(response.getTimeZoneId()).isEqualTo(TimeZones.getTimeZone("Europe/Berlin").getID());
        Assertions.assertThat(response.getLocationName()).isEqualTo("Berlin");
        Assertions.assertThat(response.getLongitude()).isEqualTo(responseByHost.getLongitude());
        Assertions.assertThat(response.getLatitude()).isEqualTo(responseByHost.getLatitude());
    }
}
