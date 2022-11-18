package de.invesdwin.webproxy.geolocation.contract.internal.service;

import javax.annotation.concurrent.ThreadSafe;
import jakarta.inject.Inject;

import org.springframework.integration.annotation.ServiceActivator;

import de.invesdwin.context.integration.retry.RetryLaterException;
import de.invesdwin.webproxy.geolocation.contract.IGeolocationService;
import de.invesdwin.webproxy.geolocation.contract.schema.GeolocationRequest;
import de.invesdwin.webproxy.geolocation.contract.schema.GeolocationResponse;
import de.invesdwin.webproxy.geolocation.contract.schema.GeolocationResponse.RetryLaterExceptionResponse;

@ThreadSafe
public class GeolocationServiceActivator implements IGeolocationServiceEndpoint {

    @Inject
    private IGeolocationService service;

    @ServiceActivator
    @Override
    public GeolocationResponse request(final GeolocationRequest request) {
        final GeolocationResponse response = new GeolocationResponse();
        try {
            if (request.getGetGeolocationByHostRequest() != null) {
                response.setGetGeolocationResponse(service.getGeolocation(request.getGetGeolocationByHostRequest()));
            } else if (request.getGetGeolocationByCoordinatesRequest() != null) {
                response.setGetGeolocationResponse(service.getGeolocation(request.getGetGeolocationByCoordinatesRequest()));
            }
        } catch (final RetryLaterException e) {
            final RetryLaterExceptionResponse excResponse = new RetryLaterExceptionResponse();
            excResponse.setMessage(e.getMessage());
            response.setRetryLaterExceptionResponse(excResponse);
        }
        return response;
    }
}
