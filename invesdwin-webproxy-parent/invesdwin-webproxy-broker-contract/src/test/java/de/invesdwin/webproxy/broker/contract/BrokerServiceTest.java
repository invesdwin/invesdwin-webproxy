package de.invesdwin.webproxy.broker.contract;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

import org.junit.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.messaging.MessageHandlingException;

import de.invesdwin.context.integration.IntegrationProperties;
import de.invesdwin.context.integration.ws.registry.RegistryServiceStub;
import de.invesdwin.context.test.ATest;
import de.invesdwin.context.test.TestContext;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.webproxy.broker.contract.schema.BrokerRequest.AddToBeVerifiedProxiesRequest;
import de.invesdwin.webproxy.broker.contract.schema.BrokerResponse.GetTaskForCrawlerResponse;
import de.invesdwin.webproxy.broker.contract.schema.RawProxy;

@ThreadSafe
public class BrokerServiceTest extends ATest {

    @Inject
    private IBrokerService broker;

    @Override
    public void setUpContext(final TestContext ctx) throws Exception {
        super.setUpContext(ctx);
        ctx.deactivate(BrokerServiceStub.class);
        RegistryServiceStub.override("webproxy.broker",
                IntegrationProperties.WEBSERVER_BIND_URI + "/spring-ws/webproxy.broker.wsdl");
    }

    @Test
    public void testTaskForCrawler() {
        final AddToBeVerifiedProxiesRequest addRequest = new AddToBeVerifiedProxiesRequest();
        final RawProxy raw = new RawProxy();
        raw.setHost("127.0.0.1");
        raw.setPort(8080);
        addRequest.getToBeVerifiedProxies().add(raw);
        broker.addToBeVerifiedProxies(addRequest);

        final GetTaskForCrawlerResponse taskResponse = broker.getTaskForCrawler();
        Assertions.assertThat(taskResponse.getToBeVerifiedProxies().size()).isEqualTo(1);
        Assertions.assertThat(taskResponse.getToBeVerifiedProxies().get(0)).isEqualTo(raw);
        Assertions.assertThat(taskResponse.getToBeScannedPorts().size()).isGreaterThan(20);

        final GetTaskForCrawlerResponse emptyTaskResponse = broker.getTaskForCrawler();
        Assertions.assertThat(emptyTaskResponse.getToBeVerifiedProxies().size()).isZero();
        Assertions.assertThat(emptyTaskResponse.getToBeScannedPorts().size()).isGreaterThan(20);
    }

    @Test
    public void testEmptyToBeVerifiedProxies() {
        Assertions.assertThrows(MessageHandlingException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                final AddToBeVerifiedProxiesRequest addRequest = new AddToBeVerifiedProxiesRequest();
                addRequest.getToBeVerifiedProxies().clear();
                broker.addToBeVerifiedProxies(addRequest);
            }
        });
    }

}
