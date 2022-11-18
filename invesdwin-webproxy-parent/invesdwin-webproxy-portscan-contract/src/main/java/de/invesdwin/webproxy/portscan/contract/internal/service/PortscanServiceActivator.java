package de.invesdwin.webproxy.portscan.contract.internal.service;

import javax.annotation.concurrent.ThreadSafe;
import jakarta.inject.Inject;

import org.springframework.integration.annotation.ServiceActivator;

import de.invesdwin.webproxy.portscan.contract.IPortscanService;
import de.invesdwin.webproxy.portscan.contract.schema.PortscanAsyncRequest;
import de.invesdwin.webproxy.portscan.contract.schema.PortscanSyncRequest;
import de.invesdwin.webproxy.portscan.contract.schema.PortscanSyncResponse;

@ThreadSafe
public class PortscanServiceActivator implements IPortscanSyncServiceEndpoint, IPortscanAsyncServiceEndpoint {

    @Inject
    private IPortscanService service;

    @ServiceActivator
    @Override
    public PortscanSyncResponse request(final PortscanSyncRequest request) {
        final PortscanSyncResponse response = new PortscanSyncResponse();
        if (request.getStatusRequest() != null) {
            response.setStatusResponse(service.status());
        } else {
            throw new IllegalArgumentException(
                    "Programming error! This type of request is not supported synchronously: " + request);
        }
        return response;
    }

    @ServiceActivator
    @Override
    public void request(final PortscanAsyncRequest request) {
        if (request.getPingRequest() != null) {
            service.ping(request.getPingRequest());
        } else if (request.getScanRequest() != null) {
            service.scan(request.getScanRequest());
        } else if (request.getRandomScanRequest() != null) {
            service.randomScan(request.getRandomScanRequest());
        } else {
            throw new IllegalArgumentException(
                    "Programming error! This type of request is not supported asynchronously: " + request);
        }
    }

}
