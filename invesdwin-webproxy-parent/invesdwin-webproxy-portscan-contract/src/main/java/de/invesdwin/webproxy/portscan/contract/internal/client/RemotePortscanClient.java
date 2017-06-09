package de.invesdwin.webproxy.portscan.contract.internal.client;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

import de.invesdwin.webproxy.portscan.contract.IPortscanClient;
import de.invesdwin.webproxy.portscan.contract.schema.PortscanAsyncResponse;
import de.invesdwin.webproxy.portscan.contract.schema.PortscanAsyncResponse.PingResponse;
import de.invesdwin.webproxy.portscan.contract.schema.PortscanAsyncResponse.ScanResponse;

@ThreadSafe
public class RemotePortscanClient implements IPortscanClient {

    @Inject
    private IPortscanAsyncClientEndpoint endpoint;

    @Override
    public void hostIsReachable(final PingResponse response) {
        final PortscanAsyncResponse r = new PortscanAsyncResponse();
        r.setPingResponse(response);
        endpoint.respond(r);
    }

    @Override
    public void portIsReachable(final ScanResponse response) {
        final PortscanAsyncResponse r = new PortscanAsyncResponse();
        r.setScanResponse(response);
        endpoint.respond(r);
    }

}
