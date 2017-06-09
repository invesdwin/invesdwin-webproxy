package de.invesdwin.webproxy.geolocation.contract.internal.service;

import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessageEndpoint;

import de.invesdwin.webproxy.geolocation.contract.schema.GeolocationRequest;
import de.invesdwin.webproxy.geolocation.contract.schema.GeolocationResponse;

@MessageEndpoint
public interface IGeolocationServiceEndpoint {

    @Gateway
    GeolocationResponse request(GeolocationRequest request);

}
