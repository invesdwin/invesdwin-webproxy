package de.invesdwin.webproxy.portscan.contract;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Named;

import de.invesdwin.context.test.ATest;
import de.invesdwin.context.test.TestContext;
import de.invesdwin.context.test.stub.StubSupport;
import de.invesdwin.util.collections.Iterables;
import de.invesdwin.util.lang.uri.Addresses;
import de.invesdwin.webproxy.portscan.contract.schema.PortscanAsyncRequest.PingRequest;
import de.invesdwin.webproxy.portscan.contract.schema.PortscanAsyncRequest.RandomScanRequest;
import de.invesdwin.webproxy.portscan.contract.schema.PortscanAsyncRequest.ScanRequest;
import de.invesdwin.webproxy.portscan.contract.schema.PortscanAsyncResponse.PingResponse;
import de.invesdwin.webproxy.portscan.contract.schema.PortscanAsyncResponse.ScanResponse;
import de.invesdwin.webproxy.portscan.contract.schema.PortscanSyncResponse.StatusResponse;
import de.invesdwin.webproxy.portscan.contract.schema.RandomScan;

@ThreadSafe
@Named
public class PortscanServiceStub extends StubSupport implements IPortscanService {

    private IPortscanClient client;

    private volatile boolean randomScan;

    @Override
    public void setUpContext(final ATest test, final TestContext ctx) throws Exception {
        super.setUpContext(test, ctx);
        ctx.replaceBean(IPortscanService.class, this.getClass());
    }

    @Override
    public void setUpOnce(final ATest test, final TestContext ctx) throws Exception {
        super.setUpOnce(test, ctx);
        client = ctx.getBean(IPortscanClient.class);
    }

    /**
     * Returns max 10 responses to the client.
     */
    @Override
    public void scan(final ScanRequest request) {
        final Set<Integer> respondingPorts = new LinkedHashSet<Integer>();
        if (request.getToBeScannedPorts().size() > 0) {
            respondingPorts.addAll(request.getToBeScannedPorts());
        } else {
            respondingPorts.addAll(Addresses.ALL_PORTS);
        }
        for (final Integer port : Iterables.limit(respondingPorts, 10)) {
            final ScanResponse response = new ScanResponse();
            response.setScannedHost(request.getToBeScannedHost());
            response.setRespondingPort(port);
            client.portIsReachable(response);
        }
    }

    /**
     * Directly responses to the client.
     */
    @Override
    public void ping(final PingRequest request) {
        final PingResponse response = new PingResponse();
        response.setRespondingHost(request.getToBePingedHost());
        client.hostIsReachable(response);
    }

    @Override
    public void randomScan(final RandomScanRequest request) {
        randomScan = request.getStartOrStop().equals(RandomScan.START);
    }

    @Override
    public StatusResponse status() {
        final StatusResponse response = new StatusResponse();
        response.setRandomScanActive(randomScan);
        response.setRequestProcessingActive(false);
        return response;
    }

}
