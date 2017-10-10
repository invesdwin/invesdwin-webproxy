package de.invesdwin.webproxy.geolocation.internal.geoip;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

import org.junit.Test;

import com.maxmind.geoip.Location;

import de.invesdwin.context.integration.retry.RetryLaterException;
import de.invesdwin.context.persistence.jpa.test.APersistenceTest;
import de.invesdwin.context.test.TestContext;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.lang.uri.Addresses;

@ThreadSafe
public class GeoIPServiceTest extends APersistenceTest {

    @Inject
    private IGeoIPService geoip;

    @Override
    public void setUpContext(final TestContext ctx) throws Exception {
        super.setUpContext(ctx);
        ctx.deactivate(GeoIPServiceStub.class);
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
