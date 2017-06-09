package de.invesdwin.webproxy.portscan.contract.internal.client;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

import org.springframework.integration.annotation.ServiceActivator;

import de.invesdwin.webproxy.portscan.contract.IPortscanClient;
import de.invesdwin.webproxy.portscan.contract.schema.PortscanAsyncResponse;

@ThreadSafe
public class PortscanClientActivator implements IPortscanAsyncClientEndpoint {

    @Inject
    private IPortscanClient client;

    @ServiceActivator
    @Override
    public void respond(final PortscanAsyncResponse response) {
        if (response.getPingResponse() != null) {
            client.hostIsReachable(response.getPingResponse());
        } else if (response.getScanResponse() != null) {
            client.portIsReachable(response.getScanResponse());
        }
    }

}
