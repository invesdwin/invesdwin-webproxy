package de.invesdwin.webproxy.portscan.internal;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import de.invesdwin.context.test.ATest;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.webproxy.portscan.contract.IPortscanClient;
import de.invesdwin.webproxy.portscan.contract.IPortscanService;
import de.invesdwin.webproxy.portscan.contract.schema.PortscanAsyncRequest.PingRequest;
import de.invesdwin.webproxy.portscan.contract.schema.PortscanAsyncRequest.RandomScanRequest;
import de.invesdwin.webproxy.portscan.contract.schema.PortscanAsyncRequest.ScanRequest;
import de.invesdwin.webproxy.portscan.contract.schema.PortscanAsyncResponse.PingResponse;
import de.invesdwin.webproxy.portscan.contract.schema.PortscanAsyncResponse.ScanResponse;
import de.invesdwin.webproxy.portscan.contract.schema.RandomScan;

@ThreadSafe
public class PortScanServiceTest extends ATest {

    private static final String TEST_ADDRESS = "google.de";

    @Inject
    @InjectMocks
    private IPortscanService service;
    @Mock
    private IPortscanClient clientMock;

    @Test
    public void testScan() throws InterruptedException {
        final ScanRequest request = new ScanRequest();
        request.setToBeScannedHost(TEST_ADDRESS);
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
        request.setStartOrStop(RandomScan.STOP);
        service.randomScan(request);
    }

    @Test
    public void testStatus() {
        Assertions.assertThat(service.status()).isNotNull();
    }

}
