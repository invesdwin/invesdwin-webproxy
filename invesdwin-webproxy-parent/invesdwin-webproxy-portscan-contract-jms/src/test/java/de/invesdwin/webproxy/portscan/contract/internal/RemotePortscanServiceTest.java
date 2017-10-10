package de.invesdwin.webproxy.portscan.contract.internal;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

import org.junit.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import de.invesdwin.context.test.ATest;
import de.invesdwin.context.test.TestContext;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.lang.uri.Addresses;
import de.invesdwin.util.time.duration.Duration;
import de.invesdwin.util.time.fdate.FTimeUnit;
import de.invesdwin.webproxy.portscan.contract.IPortscanClient;
import de.invesdwin.webproxy.portscan.contract.IPortscanService;
import de.invesdwin.webproxy.portscan.contract.PortscanServiceStub;
import de.invesdwin.webproxy.portscan.contract.internal.client.PortscanClientActivator;
import de.invesdwin.webproxy.portscan.contract.schema.PortscanAsyncRequest.PingRequest;
import de.invesdwin.webproxy.portscan.contract.schema.PortscanAsyncRequest.RandomScanRequest;
import de.invesdwin.webproxy.portscan.contract.schema.PortscanAsyncRequest.ScanRequest;
import de.invesdwin.webproxy.portscan.contract.schema.PortscanAsyncResponse.PingResponse;
import de.invesdwin.webproxy.portscan.contract.schema.PortscanAsyncResponse.ScanResponse;
import de.invesdwin.webproxy.portscan.contract.schema.RandomScan;

@ThreadSafe
public class RemotePortscanServiceTest extends ATest {

    private static final String TEST_ADDRESS = Addresses.asAddress("invesdwin.de").getHostAddress();

    @Inject
    private IPortscanService service;
    @Mock
    private IPortscanClient clientMock;
    @Inject
    @InjectMocks
    private PortscanClientActivator activatorNeedsMock;

    @Override
    public void setUpContext(final TestContext ctx) throws Exception {
        super.setUpContext(ctx);
        ctx.deactivate(PortscanServiceStub.class);
    }

    @Test
    public void testScan() throws InterruptedException {
        final ScanRequest request = new ScanRequest();
        request.setToBeScannedHost(Addresses.asAddress(TEST_ADDRESS).getHostAddress());
        request.getToBeScannedPorts().add(80);
        service.scan(request);
        Mockito.verify(clientMock, Mockito.timeout(3000)).portIsReachable(Mockito.any(ScanResponse.class));
        Mockito.verifyNoMoreInteractions(clientMock);
    }

    @Test
    public void testPing() throws InterruptedException {
        final PingRequest request = new PingRequest();
        request.setToBePingedHost(TEST_ADDRESS);
        service.ping(request);
        Mockito.verify(clientMock, Mockito.timeout(3000)).hostIsReachable(Mockito.any(PingResponse.class));
        Mockito.verifyNoMoreInteractions(clientMock);
    }

    @Test
    public void testRandomScan() throws InterruptedException {
        final RandomScanRequest request = new RandomScanRequest();
        request.setStartOrStop(RandomScan.START);
        request.getToBeScannedPorts().add(80);
        service.randomScan(request);
        Mockito.verify(clientMock, Mockito.timeout(30000)).portIsReachable(Mockito.any(ScanResponse.class));
        Mockito.verifyNoMoreInteractions(clientMock);
    }

    @Test
    public void testStatus() {
        Assertions.assertTimeout(new Duration(3, FTimeUnit.SECONDS), new Executable() {
            @Override
            public void execute() throws Throwable {
                Assertions.assertThat(service.status()).isNotNull();
            }
        });
    }

}
