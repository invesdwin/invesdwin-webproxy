package de.invesdwin.webproxy.portscan.contract.internal.service;

import javax.annotation.concurrent.ThreadSafe;
import jakarta.inject.Inject;

import de.invesdwin.webproxy.portscan.contract.IPortscanService;
import de.invesdwin.webproxy.portscan.contract.schema.PortscanAsyncRequest;
import de.invesdwin.webproxy.portscan.contract.schema.PortscanAsyncRequest.PingRequest;
import de.invesdwin.webproxy.portscan.contract.schema.PortscanAsyncRequest.RandomScanRequest;
import de.invesdwin.webproxy.portscan.contract.schema.PortscanAsyncRequest.ScanRequest;
import de.invesdwin.webproxy.portscan.contract.schema.PortscanSyncRequest;
import de.invesdwin.webproxy.portscan.contract.schema.PortscanSyncRequest.StatusRequest;
import de.invesdwin.webproxy.portscan.contract.schema.PortscanSyncResponse.StatusResponse;

@ThreadSafe
public class RemotePortscanService implements IPortscanService {

    @Inject
    private IPortscanSyncServiceEndpoint syncEndpoint;
    @Inject
    private IPortscanAsyncServiceEndpoint asyncEndpoint;

    @Override
    public void ping(final PingRequest request) {
        final PortscanAsyncRequest r = new PortscanAsyncRequest();
        r.setPingRequest(request);
        asyncEndpoint.request(r);
    }

    @Override
    public void randomScan(final RandomScanRequest request) {
        final PortscanAsyncRequest r = new PortscanAsyncRequest();
        r.setRandomScanRequest(request);
        asyncEndpoint.request(r);
    }

    @Override
    public void scan(final ScanRequest request) {
        final PortscanAsyncRequest r = new PortscanAsyncRequest();
        r.setScanRequest(request);
        asyncEndpoint.request(r);
    }

    @Override
    public StatusResponse status() {
        final PortscanSyncRequest r = new PortscanSyncRequest();
        r.setStatusRequest(new StatusRequest());
        return syncEndpoint.request(r).getStatusResponse();
    }

}
