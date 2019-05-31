package de.invesdwin.webproxy.geolocation;

import javax.annotation.concurrent.NotThreadSafe;

import org.junit.Test;

import de.invesdwin.context.integration.IntegrationProperties;
import de.invesdwin.context.persistence.jpa.test.APersistenceTest;
import de.invesdwin.context.test.TestContext;
import de.invesdwin.context.webserver.test.WebserverTest;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.lang.uri.URIs;
import de.invesdwin.webproxy.geolocation.internal.geonames.GeoNamesServiceStub;

@NotThreadSafe
//@PersistenceTest(PersistenceTestContext.SERVER)
@WebserverTest
public class ServerTest extends APersistenceTest {

    @Override
    public void setUpContext(final TestContext ctx) throws Exception {
        super.setUpContext(ctx);
        ctx.deactivateBean(GeoNamesServiceStub.class);
        //        ctx.deactivate(RegistryServiceMock.class);
    }

    @Test
    public void testServiceRunning() throws InterruptedException {
        final String wsdl = URIs
                .connect(IntegrationProperties.WEBSERVER_BIND_URI + "/spring-ws/webproxy.geolocation.wsdl").download();
        Assertions.assertThat(wsdl).contains("<wsdl:service name=\"webproxy.geolocationService\"");
    }

}
