package de.invesdwin.webproxy;

import java.net.URI;
import java.util.concurrent.ExecutionException;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import javax.inject.Named;

import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.concurrent.Threads;
import de.invesdwin.util.error.Throwables;
import de.invesdwin.util.lang.Strings;
import de.invesdwin.webproxy.broker.contract.CheckClient;
import de.invesdwin.webproxy.broker.contract.IBrokerService;
import de.invesdwin.webproxy.broker.contract.ProxyUtil;
import de.invesdwin.webproxy.broker.contract.schema.BrokerRequest.AddToBeVerifiedProxiesRequest;
import de.invesdwin.webproxy.broker.contract.schema.Proxy;
import de.invesdwin.webproxy.broker.contract.schema.ProxyQuality;
import de.invesdwin.webproxy.callbacks.AProxyResponseCallback;
import de.invesdwin.webproxy.callbacks.statistics.ConsoleReportStatisticsCallback;
import de.invesdwin.webproxy.internal.get.string.FixedProxyGetString;
import de.invesdwin.webproxy.internal.proxypool.PooledProxy;

@Named
@ThreadSafe
public class ProxyVerification {

    private final ConsoleReportStatisticsCallback statisticsCallback = new ConsoleReportStatisticsCallback();

    private final AProxyResponseCallback proxyResponseCallback = new AProxyResponseCallback() {
        @Override
        public boolean isValidResponse(final URI uri, final String response, final Object originalResponse) {
            if (Strings.containsIgnoreCase(response, "redirect")) {
                //If a lock lies on the proxy for a few seconds until the splash page splash page goes away
                try {
                    WebproxyProperties.PROXY_VERIFICATION_REDIRECT_SLEEP.sleep();
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return false;
            } else {
                return ProxyUtil.valueOf(response) != null;
            }
        }
    };

    @Inject
    private IBrokerService broker;
    @Inject
    private CheckClient checkClient;

    public ConsoleReportStatisticsCallback getStatisticsCallback() {
        return statisticsCallback;
    }

    /**
     * This method is being called before a proxy is being used to work around proxy splash pages and to update the
     * lastSuccessful timestamp on the proxy. And if a download fails to check if the proxy is the cause of it or the
     * request for it is incorrect.
     * 
     * SIDE EFFECT: Updates the ProxyQuality of that proxy instance.
     * 
     */
    public boolean verifyProxy(final Proxy proxy, final boolean notifyNotWorking, final ProxyQuality minQuality)
            throws InterruptedException {
        if (proxy == null) {
            return false;
        }
        Assertions.assertThat(proxy.getHost()).isNotBlank();
        Assertions.assertThat(proxy.getPort()).isNotNull();
        Assertions.assertThat(proxy.getType()).isNotNull();

        final FixedProxyGetString get = new FixedProxyGetString(proxy, true);
        final GetStringConfig config = new GetStringConfig();
        config.withStatisticsCallback(statisticsCallback);
        config.withProxyResponseCallback(proxyResponseCallback);
        //Retries get handled here outside
        config.withMaxDownloadRetries(0);
        final URI checkUri = checkClient.getCheckProxyQualityUri();
        for (int i = 0; i < WebproxyProperties.DEFAULT_MAX_DOWNLOAD_RETRIES; i++) {
            Threads.throwIfInterrupted();

            final String response;
            try {
                response = get.get(config, checkUri).get();
            } catch (final ExecutionException e) {
                /*
                 * Temporary exceptions might return a wrong result, thus we still try again. By trial and error we
                 * identified that a continue here is more rubust than a break. The performance loss by longer
                 * verfications is irrelevant.
                 * 
                 * E.g.: Connections just did not come through anymore. With break 277 of 500 being detected as working,
                 * with continue 299 were detected as working. This was tested on the same proxy list.
                 */
                if (Throwables.isCausedByType(e, IllegalProxyResponseException.class)
                        || WebproxyProperties.PROXY_VERIFICATION_RETRY_ON_ALL_EXCEPTIONS) {
                    continue;
                } else {
                    break;
                }
            }

            final ProxyQuality quality = ProxyUtil.valueOf(response);
            proxy.setQuality(quality);
            if (isOfMinProxyQuality(proxy, minQuality)) {
                if (proxy instanceof PooledProxy) {
                    ((PooledProxy) proxy).setWarmedUp();
                }
                return true;
            }
        }

        //We were not able to get the desired result :/
        proxy.setQuality(null);
        if (notifyNotWorking) {
            //notifiy broker about not working proxies
            final AddToBeVerifiedProxiesRequest request = new AddToBeVerifiedProxiesRequest();
            request.getToBeVerifiedProxies().add(ProxyUtil.toRawProxy(proxy));
            broker.addToBeVerifiedProxies(request);
        }
        return false;
    }

    public boolean isOfMinProxyQuality(final Proxy proxy, final ProxyQuality minQuality) {
        Assertions.assertThat(proxy).isNotNull();
        Assertions.assertThat(minQuality).isNotNull();
        if (proxy.getQuality() == null) {
            return false;
        }
        return minQuality.ordinal() <= proxy.getQuality().ordinal();
    }

}
