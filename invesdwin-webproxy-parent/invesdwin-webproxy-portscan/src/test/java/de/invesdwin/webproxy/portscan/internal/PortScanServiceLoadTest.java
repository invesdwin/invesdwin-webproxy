package de.invesdwin.webproxy.portscan.internal;

import java.util.concurrent.TimeUnit;

import javax.annotation.concurrent.ThreadSafe;

import org.junit.jupiter.api.Test;

import de.invesdwin.context.test.ATest;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.lang.uri.Addresses;
import de.invesdwin.webproxy.portscan.contract.IPortscanService;
import de.invesdwin.webproxy.portscan.contract.schema.PortscanAsyncRequest.RandomScanRequest;
import de.invesdwin.webproxy.portscan.contract.schema.RandomScan;
import jakarta.inject.Inject;

@ThreadSafe
public class PortScanServiceLoadTest extends ATest {

    @Inject
    private IPortscanService service;

    @Test
    public void lastTest() throws InterruptedException {
        final RandomScanRequest request = new RandomScanRequest();
        request.setStartOrStop(RandomScan.START);
        request.getToBeScannedPorts().addAll(Addresses.getAllPorts());
        service.randomScan(request);
        while (true) {
            TimeUnit.MILLISECONDS.sleep(100);
        }
    }

    @Test
    public void testStatus() {
        Assertions.assertThat(service.status()).isNotNull();
    }

}
