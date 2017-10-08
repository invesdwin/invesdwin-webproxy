package de.invesdwin.webproxy.broker;

import java.net.URI;

import javax.annotation.concurrent.Immutable;

import org.junit.jupiter.api.Test;
import org.springframework.ws.client.support.destination.DestinationProvider;

import de.invesdwin.context.integration.IntegrationProperties;
import de.invesdwin.context.persistence.jpa.test.APersistenceTest;
import de.invesdwin.context.test.TestContext;
import de.invesdwin.context.webserver.test.WebserverTest;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.lang.uri.URIs;
import de.invesdwin.webproxy.broker.contract.BrokerServiceStub;
import de.invesdwin.webproxy.broker.contract.CheckClient;
import de.invesdwin.webproxy.broker.contract.schema.ProxyQuality;

@Immutable
@WebserverTest
public class ServerTest extends APersistenceTest {

    @Override
    public void setUpContext(final TestContext ctx) throws Exception {
        super.setUpContext(ctx);
        ctx.deactivate(BrokerServiceStub.class);
        //        ctx.deactivate(RegistryServiceMock.class);
    }

    @Test
    public void testServiceRunning() throws InterruptedException {
        final String wsdl = URIs.connect(IntegrationProperties.WEBSERVER_BIND_URI + "/spring-ws/webproxy.broker.wsdl")
                .download();
        Assertions.assertThat(wsdl).contains("<wsdl:service name=\"webproxy.brokerService\"");
    }

    @Test
    public void testCheckClientIp() {
        final CheckClient checkClient = newCheckClient();
        final String dl = URIs.connect(checkClient.getCheckClientIpUri()).download();
        Assertions.assertThat(dl).isEqualTo("127.0.0.1");
    }

    private CheckClient newCheckClient() {
        final CheckClient checkClient = new CheckClient();
        checkClient.setDestinationProvider(new DestinationProvider() {
            @Override
            public URI getDestination() {
                return IntegrationProperties.WEBSERVER_BIND_URI;
            }
        });
        return checkClient;
    }

    @Test
    public void testCheckProxyQuality() {
        final CheckClient checkClient = new CheckClient();
        checkClient.setDestinationProvider(new DestinationProvider() {
            @Override
            public URI getDestination() {
                return IntegrationProperties.WEBSERVER_BIND_URI;
            }
        });
        final String dl = URIs.connect(checkClient.getCheckProxyQualityUri()).download();
        Assertions.assertThat(dl).isEqualTo(ProxyQuality.INVISIBLE.toString());
    }

}
