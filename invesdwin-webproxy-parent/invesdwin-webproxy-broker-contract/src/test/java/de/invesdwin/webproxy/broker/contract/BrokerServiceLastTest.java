package de.invesdwin.webproxy.broker.contract;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

import org.junit.Test;

import de.invesdwin.context.integration.IntegrationProperties;
import de.invesdwin.context.integration.network.RandomIpGenerator;
import de.invesdwin.context.integration.ws.registry.RegistryServiceStub;
import de.invesdwin.context.test.ATest;
import de.invesdwin.context.test.TestContext;
import de.invesdwin.util.lang.uri.URIs;
import de.invesdwin.webproxy.broker.contract.schema.BrokerRequest.AddToBeVerifiedProxiesRequest;
import de.invesdwin.webproxy.broker.contract.schema.RawProxy;

@ThreadSafe
public class BrokerServiceLastTest extends ATest {

    @Inject
    private IBrokerService broker;

    @Override
    public void setUpContext(final TestContext ctx) throws Exception {
        super.setUpContext(ctx);
        ctx.deactivate(BrokerServiceStub.class);
        RegistryServiceStub.override("webproxy.broker",
                URIs.asUri(IntegrationProperties.WEBSERVER_BIND_URI + "/spring-ws/webproxy.broker.wsdl"));
    }

    @Test
    public void testAddToBeVerifiedProxies() {
        final AddToBeVerifiedProxiesRequest addRequest = new AddToBeVerifiedProxiesRequest();
        for (int i = 0; i < 10000; i++) {
            final RawProxy raw = new RawProxy();
            raw.setHost(RandomIpGenerator.getRandomIp().getHostAddress());
            raw.setPort(1);
            addRequest.getToBeVerifiedProxies().add(raw);
        }
        log.info("sending");
        broker.addToBeVerifiedProxies(addRequest);
        log.info("finished");
    }

}
