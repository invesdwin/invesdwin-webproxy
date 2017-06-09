package de.invesdwin.webproxy.geolocation.contract.internal;

import java.util.Arrays;
import java.util.List;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Named;

import org.springframework.core.io.ClassPathResource;

import de.invesdwin.context.beans.init.locations.ABeanDependantContextLocation;
import de.invesdwin.context.beans.init.locations.PositionedResource;
import de.invesdwin.webproxy.geolocation.contract.IGeolocationService;

@Named
@ThreadSafe
public class GeolocationContextLocation extends ABeanDependantContextLocation {

    @Override
    protected Class<?> getDependantBeanType() {
        return IGeolocationService.class;
    }

    @Override
    protected List<PositionedResource> getContextResourcesIfBeanExists() {
        return Arrays.asList(PositionedResource.of(new ClassPathResource(
                "/META-INF/ctx.webproxy.geolocation.service.xml")));
    }

    @Override
    protected List<PositionedResource> getContextResourcesIfBeanNotExists() {
        return Arrays.asList(PositionedResource.of(new ClassPathResource(
                "/META-INF/ctx.webproxy.geolocation.client.xml")));
    }
}
