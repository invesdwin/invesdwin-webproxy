package de.invesdwin.webproxy.geolocation.contract.internal.service;

import javax.annotation.concurrent.ThreadSafe;
import jakarta.inject.Inject;

import de.invesdwin.context.integration.retry.RetryLaterException;
import de.invesdwin.webproxy.geolocation.contract.schema.GeolocationRequest;
import de.invesdwin.webproxy.geolocation.contract.schema.GeolocationRequest.GetGeolocationByCoordinatesRequest;
import de.invesdwin.webproxy.geolocation.contract.schema.GeolocationRequest.GetGeolocationByHostRequest;
import de.invesdwin.webproxy.geolocation.contract.schema.GeolocationResponse;
import de.invesdwin.webproxy.geolocation.contract.schema.GeolocationResponse.GetGeolocationResponse;

@ThreadSafe
public class RemoteGeolocationService implements de.invesdwin.webproxy.geolocation.contract.IGeolocationService {

    @Inject
    private IGeolocationServiceEndpoint endpoint;

    @Override
    public GetGeolocationResponse getGeolocation(final GetGeolocationByHostRequest request) throws RetryLaterException {
        final GeolocationRequest r = new GeolocationRequest();
        r.setGetGeolocationByHostRequest(request);
        return requestWithExceptionHandling(r).getGetGeolocationResponse();
    }

    @Override
    public GetGeolocationResponse getGeolocation(final GetGeolocationByCoordinatesRequest request)
            throws RetryLaterException {
        final GeolocationRequest r = new GeolocationRequest();
        r.setGetGeolocationByCoordinatesRequest(request);
        return requestWithExceptionHandling(r).getGetGeolocationResponse();
    }

    private GeolocationResponse requestWithExceptionHandling(final GeolocationRequest request)
            throws RetryLaterException {
        final GeolocationResponse response = endpoint.request(request);
        if (response.getRetryLaterExceptionResponse() != null) {
            throw new RetryLaterException(response.getRetryLaterExceptionResponse().getMessage());
        } else {
            return response;
        }
    }

}
