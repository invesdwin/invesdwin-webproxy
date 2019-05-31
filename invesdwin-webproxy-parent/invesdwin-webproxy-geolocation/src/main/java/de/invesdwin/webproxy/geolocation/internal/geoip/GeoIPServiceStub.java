package de.invesdwin.webproxy.geolocation.internal.geoip;

import java.util.Locale;

import javax.annotation.concurrent.Immutable;
import javax.inject.Named;

import com.maxmind.geoip.Location;

import de.invesdwin.context.test.ATest;
import de.invesdwin.context.test.TestContext;
import de.invesdwin.context.test.stub.StubSupport;

@Immutable
@Named
public class GeoIPServiceStub extends StubSupport implements IGeoIPService {

    @Override
    public void setUpContext(final ATest test, final TestContext ctx) {
        ctx.replaceBean(IGeoIPService.class, this.getClass());
    }

    @Override
    public Location getLocation(final String ip) {
        final Location loc = new Location();
        loc.countryCode = Locale.getDefault().getCountry();
        loc.countryName = Locale.getDefault().getDisplayCountry();
        loc.city = "Berlin";
        loc.latitude = 47.55519F;
        loc.longitude = 7.79461F;
        return loc;
    }

}
