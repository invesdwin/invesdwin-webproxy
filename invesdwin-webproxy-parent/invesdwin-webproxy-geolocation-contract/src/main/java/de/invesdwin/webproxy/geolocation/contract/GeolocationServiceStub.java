package de.invesdwin.webproxy.geolocation.contract;

import java.util.Locale;
import java.util.TimeZone;

import javax.annotation.concurrent.ThreadSafe;

import de.invesdwin.context.integration.retry.RetryLaterException;
import de.invesdwin.context.test.ATest;
import de.invesdwin.context.test.ITestContextSetup;
import de.invesdwin.context.test.stub.StubSupport;
import de.invesdwin.webproxy.geolocation.contract.schema.GeolocationRequest.GetGeolocationByCoordinatesRequest;
import de.invesdwin.webproxy.geolocation.contract.schema.GeolocationRequest.GetGeolocationByHostRequest;
import de.invesdwin.webproxy.geolocation.contract.schema.GeolocationResponse.GetGeolocationResponse;
import jakarta.inject.Named;

@Named
@ThreadSafe
public class GeolocationServiceStub extends StubSupport implements IGeolocationService {

    @Override
    public void setUpContext(final ATest test, final ITestContextSetup ctx) throws Exception {
        super.setUpContext(test, ctx);
        ctx.replaceBean(IGeolocationService.class, this.getClass());
    }

    @Override
    public GetGeolocationResponse getGeolocation(final GetGeolocationByHostRequest request) throws RetryLaterException {
        final GetGeolocationByCoordinatesRequest byKoordinatenRequest = new GetGeolocationByCoordinatesRequest();
        byKoordinatenRequest.setLatitude(0F);
        byKoordinatenRequest.setLongitude(0F);
        return getGeolocation(byKoordinatenRequest);
    }

    @Override
    public GetGeolocationResponse getGeolocation(final GetGeolocationByCoordinatesRequest request)
            throws RetryLaterException {
        final GetGeolocationResponse response = new GetGeolocationResponse();
        response.setLatitude(request.getLatitude());
        response.setLongitude(request.getLongitude());
        response.setCountryCode(Locale.getDefault().getCountry());
        response.setTimeZoneId(TimeZone.getDefault().getID());
        response.setLocationName("Irgendwodorf");
        return response;
    }

}
