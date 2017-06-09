package de.invesdwin.webproxy.crawler.verification;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import javax.inject.Named;

import de.invesdwin.context.integration.retry.Retry;
import de.invesdwin.context.integration.retry.RetryLaterException;
import de.invesdwin.context.integration.retry.RetryLaterRuntimeException;
import de.invesdwin.webproxy.ProxyVerification;
import de.invesdwin.webproxy.broker.contract.ProxyUtil;
import de.invesdwin.webproxy.broker.contract.schema.Proxy;
import de.invesdwin.webproxy.broker.contract.schema.ProxyQuality;
import de.invesdwin.webproxy.broker.contract.schema.ProxyType;
import de.invesdwin.webproxy.broker.contract.schema.RawProxy;
import de.invesdwin.webproxy.geolocation.contract.IGeolocationService;
import de.invesdwin.webproxy.geolocation.contract.schema.GeolocationRequest.GetGeolocationByHostRequest;
import de.invesdwin.webproxy.geolocation.contract.schema.GeolocationResponse.GetGeolocationResponse;

@Named
@ThreadSafe
public class ProxyVerifierDataEnricher {

    @Inject
    private ProxyVerification proxyVeri;
    @Inject
    private IGeolocationService geolocation;

    public Proxy enrich(final RawProxy rawProxy) throws InterruptedException {
        for (final ProxyType typ : ProxyType.values()) {
            final Proxy proxy = ProxyUtil.valueOf(rawProxy, typ, null, null, null);
            if (proxyVeri.verifyProxy(proxy, false, ProxyQuality.TRANSPARENT)) {
                enrichWichGeolocation(proxy);
                return proxy;
            }
        }
        return null;
    }

    @Retry
    private void enrichWichGeolocation(final Proxy proxy) {
        try {
            final GetGeolocationByHostRequest geolocationRequest = new GetGeolocationByHostRequest();
            geolocationRequest.setHost(proxy.getHost());
            final GetGeolocationResponse geolocationResponse = geolocation.getGeolocation(geolocationRequest);
            proxy.setCountryCode(geolocationResponse.getCountryCode());
            proxy.setTimeZoneId(geolocationResponse.getTimeZoneId());
        } catch (final RetryLaterException e) {
            throw new RetryLaterRuntimeException(e);
        }
    }
}
