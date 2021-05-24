package de.invesdwin.webproxy.geolocation.contract;

import java.util.Locale;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

import org.junit.Test;

import de.invesdwin.context.integration.IntegrationProperties;
import de.invesdwin.context.integration.retry.RetryLaterException;
import de.invesdwin.context.integration.ws.registry.RegistryServiceStub;
import de.invesdwin.context.test.ATest;
import de.invesdwin.context.test.TestContext;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.lang.uri.URIs;
import de.invesdwin.util.time.fdate.ftimezone.TimeZones;
import de.invesdwin.webproxy.geolocation.contract.schema.GeolocationRequest.GetGeolocationByCoordinatesRequest;
import de.invesdwin.webproxy.geolocation.contract.schema.GeolocationRequest.GetGeolocationByHostRequest;
import de.invesdwin.webproxy.geolocation.contract.schema.GeolocationResponse.GetGeolocationResponse;

@ThreadSafe
public class GeolocationServiceTest extends ATest {

    @Inject
    private IGeolocationService geolocation;

    @Override
    @Inject
    public void setUpContext(final TestContext ctx) throws Exception {
        super.setUpContext(ctx);
        RegistryServiceStub.override("webproxy.geolocation",
                URIs.asUri(IntegrationProperties.WEBSERVER_BIND_URI + "/spring-ws/webproxy.geolocation.wsdl"));
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
        Assertions.assertThat(response.getCountryCode()).isEqualTo("Berlin");
        Assertions.assertThat(response.getLongitude()).isEqualTo(responseByHost.getLongitude());
        Assertions.assertThat(response.getLatitude()).isEqualTo(responseByHost.getLatitude());
    }
}
