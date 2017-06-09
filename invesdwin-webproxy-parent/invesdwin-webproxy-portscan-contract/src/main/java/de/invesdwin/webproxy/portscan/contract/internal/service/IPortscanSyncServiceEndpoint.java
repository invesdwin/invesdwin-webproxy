package de.invesdwin.webproxy.portscan.contract.internal.service;

import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessageEndpoint;

import de.invesdwin.webproxy.portscan.contract.schema.PortscanSyncRequest;
import de.invesdwin.webproxy.portscan.contract.schema.PortscanSyncResponse;

@MessageEndpoint
public interface IPortscanSyncServiceEndpoint {

    @Gateway
    PortscanSyncResponse request(PortscanSyncRequest request);

}
