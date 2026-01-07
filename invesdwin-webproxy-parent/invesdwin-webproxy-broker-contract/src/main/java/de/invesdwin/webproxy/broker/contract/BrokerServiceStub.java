package de.invesdwin.webproxy.broker.contract;

import java.util.List;

import javax.annotation.concurrent.ThreadSafe;

import de.invesdwin.context.beans.init.locations.PositionedResource;
import de.invesdwin.context.test.ATest;
import de.invesdwin.context.test.ITestContextSetup;
import de.invesdwin.context.test.stub.StubSupport;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.webproxy.broker.contract.internal.BrokerContextLocation;
import de.invesdwin.webproxy.broker.contract.internal.InMemoryBrokerService;
import jakarta.inject.Named;

@Named
@ThreadSafe
public class BrokerServiceStub extends StubSupport {

    private static volatile boolean enabled = true;

    private static volatile boolean crawlForProxies;

    @Override
    public void setUpContextLocations(final ATest test, final List<PositionedResource> locations) throws Exception {
        if (!BrokerServiceStub.isEnabled()) {
            if (!locations.remove(BrokerContextLocation.SERVICE_CONTEXT)) {
                Assertions.checkTrue(locations.remove(BrokerContextLocation.SERVICE_CONTEXT_FALLBACK));
            }
            locations.add(BrokerContextLocation.CLIENT_CONTEXT);
        }
    }

    @Override
    public void setUpContext(final ATest test, final ITestContextSetup ctx) {
        if (BrokerServiceStub.isEnabled()) {
            ctx.replaceBean(IBrokerService.class, InMemoryBrokerService.class);
        }
    }

    public static void setEnabled(final boolean enabled) {
        BrokerServiceStub.enabled = enabled;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setCrawlForProxies(final boolean value) {
        crawlForProxies = value;
    }

    public static boolean isCrawlForProxies() {
        return crawlForProxies;
    }

}
