package de.invesdwin.webproxy.portscan.contract.internal.client;

import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessageEndpoint;

import de.invesdwin.webproxy.portscan.contract.schema.PortscanAsyncResponse;

@MessageEndpoint
public interface IPortscanAsyncClientEndpoint {

    @Gateway
    void respond(PortscanAsyncResponse response);

}
