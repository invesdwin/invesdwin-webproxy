package de.invesdwin.webproxy.geolocation.internal.geonames;

import javax.annotation.concurrent.Immutable;
import javax.inject.Named;

import de.invesdwin.context.test.ATest;
import de.invesdwin.context.test.TestContext;
import de.invesdwin.context.test.stub.StubSupport;
import de.invesdwin.webproxy.geolocation.internal.geonames.persistence.AToponymEntity;
import de.invesdwin.webproxy.geolocation.internal.geonames.persistence.redundance.ToponymEntity;

@Immutable
@Named
public class GeoNamesServiceStub extends StubSupport implements IGeoNamesService {

    @Override
    public void setUpContext(final ATest test, final TestContext ctx) {
        ctx.replaceBean(IGeoNamesService.class, this.getClass());
    }

    @Override
    public AToponymEntity getToponym(final float breitengrad, final float laengengrad) {
        final AToponymEntity ent = new ToponymEntity();
        ent.setLatitude(10F);
        ent.setLongitude(10F);
        ent.setLocationName("Berlin");
        ent.setCountryCode("DE");
        ent.setTimeZoneId("Europe/Berlin");
        return ent;
    }

}
