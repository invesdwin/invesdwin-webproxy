package de.invesdwin.webproxy.geolocation.internal.geonames;

import java.util.TimeZone;

import javax.annotation.concurrent.NotThreadSafe;
import javax.inject.Inject;

import org.junit.Test;

import com.maxmind.geoip.Location;

import de.invesdwin.context.integration.retry.RetryLaterException;
import de.invesdwin.context.persistence.jpa.test.APersistenceTest;
import de.invesdwin.context.persistence.jpa.test.PersistenceTest;
import de.invesdwin.context.persistence.jpa.test.PersistenceTestContext;
import de.invesdwin.context.test.TestContext;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.lang.uri.Addresses;
import de.invesdwin.util.time.fdate.FTimeUnit;
import de.invesdwin.util.time.fdate.ftimezone.TimeZones;
import de.invesdwin.webproxy.geolocation.internal.geoip.GeoIPServiceStub;
import de.invesdwin.webproxy.geolocation.internal.geoip.IGeoIPService;
import de.invesdwin.webproxy.geolocation.internal.geonames.persistence.AToponymEntity;

@NotThreadSafe
@PersistenceTest(PersistenceTestContext.SERVER)
public class GeoNamesServiceTest extends APersistenceTest {

    @Inject
    private IGeoIPService geoip;
    @Inject
    private IGeoNamesService geonames;

    @Override
    public void setUpOnce() throws Exception {
        super.setUpOnce();
    }

    @Override
    public void setUpContext(final TestContext ctx) throws Exception {
        super.setUpContext(ctx);
        ctx.deactivateBean(GeoIPServiceStub.class);
        ctx.deactivateBean(GeoNamesServiceStub.class);
    }

    @Test
    public void testGetToponym() throws RetryLaterException {
        final String address = Addresses.asAddress("berlin.de").getHostAddress();
        final Location location = geoip.getLocation(address);
        final AToponymEntity ent = geonames.getToponym(location.latitude, location.longitude);
        log.info("lat[%s]long[%s] -> lat[%s]long[%s] with classification [%s]", location.longitude, location.latitude,
                ent.getLongitude(), ent.getLatitude(), ent.getClassification());
        Assertions.assertThat(ent.getCountryCode()).isEqualTo("DE");
        final String zeitoneId = ent.getTimeZoneId();
        Assertions.assertThat(zeitoneId).isEqualTo("Europe/Berlin");
        final TimeZone javaTimeZone = TimeZones.getTimeZone(zeitoneId);
        //+1 hour offset for Berlin
        Assertions.assertThat(javaTimeZone.getRawOffset()).isEqualTo(
                1 * FTimeUnit.MINUTES_IN_HOUR * FTimeUnit.SECONDS_IN_MINUTE * FTimeUnit.MILLISECONDS_IN_SECOND);
        /*
         * location determination is currently very inaccurate with GeoIP because they only position by country in the
         * free data version...
         */
        Assertions.assertThat(ent.getLocationName()).isEqualTo("Hohlstein");
    }

    @Test
    public void testGetToponymLast() throws RetryLaterException {
        for (int i = 1; i <= 10; i++) {
            final AToponymEntity ent = geonames.getToponym(i, i);
            log.info("%s: %s", i, ent);
        }
    }

}
