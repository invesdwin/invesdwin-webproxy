package de.invesdwin.webproxy.portscan.contract.internal.service;

import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessageEndpoint;

import de.invesdwin.webproxy.portscan.contract.schema.PortscanAsyncRequest;

@MessageEndpoint
public interface IPortscanAsyncServiceEndpoint {

    @Gateway
    void request(PortscanAsyncRequest request);
}
