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

    public GetStringConfig setBrowserVersion(final BrowserVersion browserVersion) {
        this.browserVersion = browserVersion;
        return this;
    }

    public BrowserVersion getBrowserVersion() {
        return browserVersion;
    }

    @Override
    public GetStringConfig setMaxParallelDownloads(final Integer maxParallelDownloads) {
        super.setMaxParallelDownloads(maxParallelDownloads);
        return this;
    }

    @Override
    public GetStringConfig setProxyResponseCallback(final AProxyResponseCallback proxyResponseCallback) {
        super.setProxyResponseCallback(proxyResponseCallback);
        return this;
    }

    @Override
    public GetStringConfig setUseProxyPool(final boolean useProxyPool) {
        super.setUseProxyPool(useProxyPool);
        return this;
    }

    @Override
    public GetStringConfig setStatisticsCallback(final AStatisticsCallback statisticsCallback) {
        super.setStatisticsCallback(statisticsCallback);
        return this;
    }

    @Override
    public GetStringConfig resetVisitedUris() {
        super.resetVisitedUris();
        return this;
    }

    @Override
    public GetStringConfig setFilterVisitedUris(final boolean filterVisitedUris) {
        super.setFilterVisitedUris(filterVisitedUris);
        return this;
    }

    @Override
    public GetStringConfig setFixedProxy(final Proxy fixedProxy) {
        super.setFixedProxy(fixedProxy);
        return this;
    }

    @Override
    public GetStringConfig setSystemProxyAsFixedProxy() {
        super.setSystemProxyAsFixedProxy();
        return this;
    }

    @Override
    public GetStringConfig setMinProxyQuality(final ProxyQuality minProxyQuality) {
        super.setMinProxyQuality(minProxyQuality);
        return this;
    }

    @Override
    public GetStringConfig setMaxDownloadTryDuration(final Duration maxDownloadTryDuration) {
        super.setMaxDownloadTryDuration(maxDownloadTryDuration);
        return this;
    }

    @Override
    public GetStringConfig setMaxDownloadRetries(final int maxDownloadRetries) {
        super.setMaxDownloadRetries(maxDownloadRetries);
        return this;
    }

    @Override
    public GetStringConfig setMaxDownloadRetriesWarningOnly(final boolean maxDownloadRetriesWarningOnly) {
        super.setMaxDownloadRetriesWarningOnly(maxDownloadRetriesWarningOnly);
        return this;
    }

}
