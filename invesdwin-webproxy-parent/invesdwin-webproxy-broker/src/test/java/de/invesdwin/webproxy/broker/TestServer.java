package de.invesdwin.webproxy.broker;

import java.util.concurrent.TimeUnit;

import javax.annotation.concurrent.Immutable;

import org.junit.Test;

import de.invesdwin.context.integration.IntegrationProperties;
import de.invesdwin.context.persistence.jpa.test.APersistenceTest;
import de.invesdwin.context.test.TestContext;
import de.invesdwin.context.webserver.test.WebserverTest;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.lang.uri.URIs;
import de.invesdwin.webproxy.broker.contract.BrokerServiceStub;

@Immutable
@WebserverTest
public class TestServer extends APersistenceTest {

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
        TimeUnit.DAYS.sleep(Long.MAX_VALUE);
    }

}
