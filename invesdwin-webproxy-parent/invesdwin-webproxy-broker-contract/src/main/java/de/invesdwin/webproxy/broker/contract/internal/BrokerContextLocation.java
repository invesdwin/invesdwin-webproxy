package de.invesdwin.webproxy.broker.contract.internal;

import java.util.Arrays;
import java.util.List;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Named;

import org.springframework.core.io.ClassPathResource;

import de.invesdwin.context.ContextProperties;
import de.invesdwin.context.beans.init.locations.ABeanDependantContextLocation;
import de.invesdwin.context.beans.init.locations.PositionedResource;
import de.invesdwin.context.beans.init.locations.position.ResourcePosition;
import de.invesdwin.webproxy.broker.contract.BrokerServiceStub;
import de.invesdwin.webproxy.broker.contract.IBrokerService;

@Named
@ThreadSafe
public class BrokerContextLocation extends ABeanDependantContextLocation {

    public static final PositionedResource CLIENT_CONTEXT = PositionedResource
            .of(new ClassPathResource("/META-INF/ctx.webproxy.broker.client.xml"));
    public static final PositionedResource SERVICE_CONTEXT = PositionedResource
            .of(new ClassPathResource("/META-INF/ctx.webproxy.broker.service.xml"), ResourcePosition.START);
    public static final PositionedResource SERVICE_CONTEXT_FALLBACK = PositionedResource
            .of(new ClassPathResource("/META-INF/actx.webproxy.broker.xml"), ResourcePosition.START);

    @Override
    protected Class<?> getDependantBeanType() {
        return IBrokerService.class;
    }

    @Override
    protected List<PositionedResource> getContextResourcesIfBeanExists() {
        if (ContextProperties.IS_TEST_ENVIRONMENT && BrokerServiceStub.isEnabled()
                && ctx.getBeanNamesForType(BrokerServiceStub.class).length != 0) {
            //disable
            return Arrays.asList(SERVICE_CONTEXT_FALLBACK);
        } else {
            //context needs to be at the beginning to be able to override
            return Arrays.asList(SERVICE_CONTEXT);
        }
    }

    @Override
    protected List<PositionedResource> getContextResourcesIfBeanNotExists() {
        return Arrays.asList(CLIENT_CONTEXT);
    }
}
