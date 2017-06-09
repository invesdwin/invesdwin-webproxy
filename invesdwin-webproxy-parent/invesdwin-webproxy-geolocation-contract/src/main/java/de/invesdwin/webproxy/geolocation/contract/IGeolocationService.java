package de.invesdwin.webproxy.geolocation.contract;

import de.invesdwin.context.integration.retry.RetryLaterException;
import de.invesdwin.webproxy.geolocation.contract.schema.GeolocationRequest.GetGeolocationByCoordinatesRequest;
import de.invesdwin.webproxy.geolocation.contract.schema.GeolocationRequest.GetGeolocationByHostRequest;
import de.invesdwin.webproxy.geolocation.contract.schema.GeolocationResponse.GetGeolocationResponse;

public interface IGeolocationService {

    GetGeolocationResponse getGeolocation(GetGeolocationByHostRequest request) throws RetryLaterException;

    GetGeolocationResponse getGeolocation(GetGeolocationByCoordinatesRequest request) throws RetryLaterException;

}