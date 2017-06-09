package de.invesdwin.webproxy.broker.contract.internal.service;

import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessageEndpoint;

import de.invesdwin.webproxy.broker.contract.schema.BrokerRequest;
import de.invesdwin.webproxy.broker.contract.schema.BrokerResponse;

@MessageEndpoint
public interface IBrokerServiceEndpoint {

    @Gateway
    BrokerResponse request(BrokerRequest request);

}
