package de.invesdwin.webproxy;

import javax.annotation.concurrent.NotThreadSafe;

import com.gargoylesoftware.htmlunit.BrowserVersion;

import de.invesdwin.util.time.duration.Duration;
import de.invesdwin.webproxy.broker.contract.schema.Proxy;
import de.invesdwin.webproxy.broker.contract.schema.ProxyQuality;
import de.invesdwin.webproxy.callbacks.AProxyResponseCallback;
import de.invesdwin.webproxy.callbacks.statistics.basis.AStatisticsCallback;
import de.invesdwin.webproxy.internal.get.AGetConfig;

@NotThreadSafe
public class GetStringConfig extends AGetConfig {

    private static final long serialVersionUID = 1L;

    private BrowserVersion browserVersion = WebproxyProperties.DEFAULT_BROWSER_VERION;

    public GetStringConfig withBrowserVersion(final BrowserVersion browserVersion) {
        this.browserVersion = browserVersion;
        return this;
    }

    public BrowserVersion getBrowserVersion() {
        return browserVersion;
    }

    @Override
    public GetStringConfig withMaxParallelDownloads(final Integer maxParallelDownloads) {
        super.withMaxParallelDownloads(maxParallelDownloads);
        return this;
    }

    @Override
    public GetStringConfig withProxyResponseCallback(final AProxyResponseCallback proxyResponseCallback) {
        super.withProxyResponseCallback(proxyResponseCallback);
        return this;
    }

    @Override
    public GetStringConfig withUseProxyPool(final boolean useProxyPool) {
        super.withUseProxyPool(useProxyPool);
        return this;
    }

    @Override
    public GetStringConfig withStatisticsCallback(final AStatisticsCallback statisticsCallback) {
        super.withStatisticsCallback(statisticsCallback);
        return this;
    }

    @Override
    public GetStringConfig resetVisitedUris() {
        super.resetVisitedUris();
        return this;
    }

    @Override
    public GetStringConfig withFilterVisitedUris(final boolean filterVisitedUris) {
        super.withFilterVisitedUris(filterVisitedUris);
        return this;
    }

    @Override
    public GetStringConfig withFixedProxy(final Proxy fixedProxy) {
        super.withFixedProxy(fixedProxy);
        return this;
    }

    @Override
    public GetStringConfig withSystemProxyAsFixedProxy() {
        super.withSystemProxyAsFixedProxy();
        return this;
    }

    @Override
    public GetStringConfig withMinProxyQuality(final ProxyQuality minProxyQuality) {
        super.withMinProxyQuality(minProxyQuality);
        return this;
    }

    @Override
    public GetStringConfig withMaxDownloadTryDuration(final Duration maxDownloadTryDuration) {
        super.withMaxDownloadTryDuration(maxDownloadTryDuration);
        return this;
    }

    @Override
    public GetStringConfig withMaxDownloadRetries(final int maxDownloadRetries) {
        super.withMaxDownloadRetries(maxDownloadRetries);
        return this;
    }

    @Override
    public GetStringConfig withMaxDownloadRetriesWarningOnly(final boolean maxDownloadRetriesWarningOnly) {
        super.withMaxDownloadRetriesWarningOnly(maxDownloadRetriesWarningOnly);
        return this;
    }

}
