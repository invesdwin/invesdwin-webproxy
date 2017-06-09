package de.invesdwin.webproxy.internal.get;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.concurrent.NotThreadSafe;
import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Configurable;

import de.invesdwin.context.integration.IntegrationProperties;
import de.invesdwin.context.log.error.Err;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.bean.AValueObject;
import de.invesdwin.util.time.duration.Duration;
import de.invesdwin.webproxy.ProxyVerification;
import de.invesdwin.webproxy.WebproxyProperties;
import de.invesdwin.webproxy.broker.contract.ProxyUtil;
import de.invesdwin.webproxy.broker.contract.schema.Proxy;
import de.invesdwin.webproxy.broker.contract.schema.ProxyQuality;
import de.invesdwin.webproxy.callbacks.AProxyResponseCallback;
import de.invesdwin.webproxy.callbacks.statistics.basis.AStatisticsCallback;

@NotThreadSafe
@Configurable
public abstract class AGetConfig extends AValueObject {

    private static final AtomicBoolean PROXY_RESPONSE_CALLBACK_ALREADY_WARNED = new AtomicBoolean();
    private static final long serialVersionUID = 1L;

    private final Set<URI> visitedUris = new CopyOnWriteArraySet<URI>();
    private boolean filterVisitedUris;

    private AStatisticsCallback statisticsCallback;
    private AProxyResponseCallback proxyResponseCallback;
    private boolean useProxyPool;
    private Proxy fixedProxy;
    private ProxyQuality minProxyQuality = ProxyQuality.TRANSPARENT;
    private Duration maxDownloadTryDuration = WebproxyProperties.DEFAULT_MAX_DOWNLOAD_TRY_DURATION;
    private int maxDownloadRetries = WebproxyProperties.DEFAULT_MAX_DOWNLOAD_RETRIES;
    private boolean maxDownloadRetriesWarningOnly = WebproxyProperties.DEFAULT_MAX_DOWNLOAD_RETRIES_WARNING_ONLY;
    private Integer maxParallelDownloads;

    @Inject
    private ProxyVerification proxyVeri;

    public boolean isFilterVisitedUris() {
        return filterVisitedUris;
    }

    public AGetConfig withMaxParallelDownloads(final Integer maxParallelDownloads) {
        Assertions.assertThat(maxParallelDownloads == null || maxParallelDownloads > 0)
                .as("Must be at least 1 if not null")
                .isTrue();
        this.maxParallelDownloads = maxParallelDownloads;
        return this;
    }

    public Integer getMaxParallelDownloads() {
        return maxParallelDownloads;
    }

    public AGetConfig withFilterVisitedUris(final boolean filterVisitedUris) {
        this.filterVisitedUris = filterVisitedUris;
        return this;
    }

    public AGetConfig resetVisitedUris() {
        visitedUris.clear();
        return this;
    }

    public AStatisticsCallback getStatisticsCallback() {
        return statisticsCallback;
    }

    public AGetConfig withStatisticsCallback(final AStatisticsCallback statisticsCallback) {
        this.statisticsCallback = statisticsCallback;
        return this;
    }

    public AProxyResponseCallback getProxyResponseCallback() {
        return proxyResponseCallback;
    }

    public AGetConfig withProxyResponseCallback(final AProxyResponseCallback proxyResponseCallback) {
        this.proxyResponseCallback = proxyResponseCallback;
        return this;
    }

    public boolean isUseProxyPool() {
        return useProxyPool;
    }

    public AGetConfig withUseProxyPool(final boolean useProxyPool) {
        this.useProxyPool = useProxyPool;
        return this;
    }

    public Proxy getFixedProxy() {
        return fixedProxy;
    }

    public AGetConfig withFixedProxy(final Proxy fixedProxy) {
        this.fixedProxy = fixedProxy;
        return this;
    }

    public AGetConfig withSystemProxyAsFixedProxy() {
        final java.net.Proxy systemProxyJava = IntegrationProperties.getSystemProxy();
        if (systemProxyJava != null) {
            final Proxy systemProxy = ProxyUtil.valueOf(systemProxyJava);
            withFixedProxy(systemProxy);
        }
        return this;
    }

    public ProxyQuality getMinProxyQuality() {
        return minProxyQuality;
    }

    public AGetConfig withMinProxyQuality(final ProxyQuality minProxyQuality) {
        Assertions.assertThat(minProxyQuality).isNotNull();
        this.minProxyQuality = minProxyQuality;
        return this;
    }

    public AGetConfig withMaxDownloadTryDuration(final Duration maxDownloadTryDuration) {
        this.maxDownloadTryDuration = maxDownloadTryDuration;
        return this;
    }

    public Duration getMaxDownloadTryDuration() {
        return maxDownloadTryDuration;
    }

    public AGetConfig withMaxDownloadRetries(final int maxDownloadRetries) {
        this.maxDownloadRetries = maxDownloadRetries;
        return this;
    }

    public int getMaxDownloadRetries() {
        return maxDownloadRetries;
    }

    public AGetConfig withMaxDownloadRetriesWarningOnly(final boolean maxDownloadRetriesWarningOnly) {
        this.maxDownloadRetriesWarningOnly = maxDownloadRetriesWarningOnly;
        return this;
    }

    public boolean getMaxDownloadRetriesWarningOnly() {
        return maxDownloadRetriesWarningOnly;
    }

    Collection<URI> filterVisitedUri(final Collection<URI> uris) {
        if (filterVisitedUris) {
            final Collection<URI> filteredUris = new ArrayList<URI>(uris.size());
            for (final URI uri : uris) {
                filteredUris.add(filterVisitedUri(uri));
            }
            return filteredUris;
        } else {
            return uris;
        }
    }

    URI filterVisitedUri(final URI uri) {
        if (filterVisitedUris && !visitedUris.add(uri)) {
            return null;
        } else {
            return uri;
        }
    }

    protected void validate(final boolean proxyVerification) {
        Assertions.assertThat(useProxyPool && getFixedProxy() != null)
                .as("You cannot configure a fixed proxy, when a proxy pool is to be used!")
                .isFalse();
        if (fixedProxy != null) {
            /*
             * At this place we trust that the caller has verified the proxy and warmed it up. If not, it might just
             * cause an exception on download.
             */
            Assertions.assertThat(proxyVeri.isOfMinProxyQuality(fixedProxy, minProxyQuality))
                    .as("The give proxy [%s] does not match the min quality [%s]!", fixedProxy, minProxyQuality)
                    .isTrue();
        }
        validateProxyResponseCallback(proxyVerification);
    }

    private void validateProxyResponseCallback(final boolean proxyVerification) {
        if (!PROXY_RESPONSE_CALLBACK_ALREADY_WARNED.getAndSet(true)) {
            if ((useProxyPool || getFixedProxy() != null) && proxyResponseCallback == null) {
                Err.process(new Exception("WARNING: You should use " + AProxyResponseCallback.class.getSimpleName()
                        + " when a proxy is to be used, so that the response can be guaranteed to be correct."));
            } else if (!useProxyPool && getFixedProxy() == null && !proxyVerification
                    && proxyResponseCallback != null) {
                Err.process(new Exception("WARNING: When no proxy is to be used, "
                        + AProxyResponseCallback.class.getSimpleName() + " won't be used either."));
            }
        }
    }

}
