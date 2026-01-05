package de.invesdwin.webproxy.geolocation.internal.geoip;

import javax.annotation.concurrent.ThreadSafe;

import org.junit.jupiter.api.Test;

import com.maxmind.geoip.Location;

import de.invesdwin.context.integration.retry.RetryLaterException;
import de.invesdwin.context.persistence.jpa.test.APersistenceTest;
import de.invesdwin.context.test.ITestContextSetup;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.lang.uri.Addresses;
import jakarta.inject.Inject;

@ThreadSafe
public class GeoIPServiceTest extends APersistenceTest {

    @Inject
    private IGeoIPService geoip;

    @Override
    public void setUpContext(final ITestContextSetup ctx) throws Exception {
        super.setUpContext(ctx);
        ctx.deactivateBean(GeoIPServiceStub.class);
    }

    @Test
    public void testGetLocation() throws RetryLaterException {
        final String address = Addresses.asAddress("66.171.189.82").getHostAddress();
        final Location location = geoip.getLocation(address);
        Assertions.assertThat(location).isNotNull();
        Assertions.assertThat(location.countryCode).isEqualTo("DE");
        log.info("Longitude: %s Latitude: %s", location.longitude, location.latitude);
    }

}
