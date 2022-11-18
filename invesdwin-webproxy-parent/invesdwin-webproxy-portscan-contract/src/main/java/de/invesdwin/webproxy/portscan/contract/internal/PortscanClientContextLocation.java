package de.invesdwin.webproxy.portscan.contract.internal;

import java.util.List;

import javax.annotation.concurrent.ThreadSafe;
import jakarta.inject.Named;

import org.springframework.core.io.ClassPathResource;

import de.invesdwin.context.beans.init.locations.ABeanDependantContextLocation;
import de.invesdwin.context.beans.init.locations.PositionedResource;
import de.invesdwin.util.collections.Arrays;
import de.invesdwin.webproxy.portscan.contract.IPortscanClient;

@ThreadSafe
@Named
public class PortscanClientContextLocation extends ABeanDependantContextLocation {

    @Override
    protected Class<?> getDependantBeanType() {
        return IPortscanClient.class;
    }

    @Override
    protected List<PositionedResource> getContextResourcesIfBeanExists() {
        return Arrays
                .asList(PositionedResource.of(new ClassPathResource("/META-INF/ctx.webproxy.portscan.client.xml")));
    }

    @Override
    protected List<PositionedResource> getContextResourcesIfBeanNotExists() {
        return null;
    }

}
