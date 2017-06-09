package de.invesdwin.webproxy;

import javax.annotation.concurrent.NotThreadSafe;

import com.gargoylesoftware.htmlunit.BrowserVersion;

import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.time.duration.Duration;
import de.invesdwin.webproxy.broker.contract.schema.Proxy;
import de.invesdwin.webproxy.broker.contract.schema.ProxyQuality;
import de.invesdwin.webproxy.callbacks.JavascriptWaitCallback;
import de.invesdwin.webproxy.callbacks.AProxyResponseCallback;
import de.invesdwin.webproxy.callbacks.statistics.basis.AStatisticsCallback;

@NotThreadSafe
public class GetPageConfig extends GetStringConfig {

    private static final long serialVersionUID = 1L;

    private JavascriptWaitCallback javascriptWaitCallback;
    private boolean javascriptEnabled;
    private int maxPageRefreshCount;
    private boolean cssEnabled;

    /********************** new stuff **********************/

    public JavascriptWaitCallback getJavascriptWaitCallback() {
        return javascriptWaitCallback;
    }

    public GetPageConfig withJavascriptWaitCallback(final JavascriptWaitCallback javascriptWaitCallback) {
        this.javascriptWaitCallback = javascriptWaitCallback;
        return this;
    }

    public boolean isJavascriptEnabled() {
        return javascriptEnabled;
    }

    public GetPageConfig withJavascriptEnabled(final boolean javascriptEnabled) {
        this.javascriptEnabled = javascriptEnabled;
        return this;
    }

    public int getMaxPageRefreshCount() {
        return maxPageRefreshCount;
    }

    public GetPageConfig withMaxPageRefreshCount(final int maxPageRefreshCount) {
        Assertions.assertThat(maxPageRefreshCount).isGreaterThanOrEqualTo(0);
        this.maxPageRefreshCount = maxPageRefreshCount;
        return this;
    }

    public GetPageConfig withCssEnabled(final boolean cssEnabled) {
        this.cssEnabled = cssEnabled;
        return this;
    }

    public boolean isCssEnabled() {
        return cssEnabled;
    }

    /**************** overrides ******************/

    @Override
    public GetPageConfig withMaxParallelDownloads(final Integer maxParallelDownloads) {
        super.withMaxParallelDownloads(maxParallelDownloads);
        return this;
    }

    @Override
    public GetPageConfig withProxyResponseCallback(final AProxyResponseCallback proxyResponseCallback) {
        super.withProxyResponseCallback(proxyResponseCallback);
        return this;
    }

    @Override
    public GetPageConfig withBrowserVersion(final BrowserVersion browserVersion) {
        super.withBrowserVersion(browserVersion);
        return this;
    }

    @Override
    public GetPageConfig withStatisticsCallback(final AStatisticsCallback statisticsCallback) {
        super.withStatisticsCallback(statisticsCallback);
        return this;
    }

    @Override
    public GetPageConfig resetVisitedUris() {
        super.resetVisitedUris();
        return this;
    }

    @Override
    public GetPageConfig withFilterVisitedUris(final boolean filterVisitedUris) {
        super.withFilterVisitedUris(filterVisitedUris);
        return this;
    }

    @Override
    public GetPageConfig withUseProxyPool(final boolean useProxy) {
        super.withUseProxyPool(useProxy);
        return this;
    }

    @Override
    public GetPageConfig withFixedProxy(final Proxy fixedProxy) {
        super.withFixedProxy(fixedProxy);
        return this;
    }

    @Override
    public GetPageConfig withSystemProxyAsFixedProxy() {
        super.withSystemProxyAsFixedProxy();
        return this;
    }

    @Override
    public GetPageConfig withMinProxyQuality(final ProxyQuality minProxyQuality) {
        super.withMinProxyQuality(minProxyQuality);
        return this;
    }

    @Override
    public GetPageConfig withMaxDownloadTryDuration(final Duration maxDownloadTryDuration) {
        super.withMaxDownloadTryDuration(maxDownloadTryDuration);
        return this;
    }

    @Override
    public GetPageConfig withMaxDownloadRetries(final int maxDownloadRetries) {
        super.withMaxDownloadRetries(maxDownloadRetries);
        return this;
    }

    @Override
    public GetPageConfig withMaxDownloadRetriesWarningOnly(final boolean maxDownloadRetriesWarningOnly) {
        super.withMaxDownloadRetriesWarningOnly(maxDownloadRetriesWarningOnly);
        return this;
    }

    @Override
    protected void validate(final boolean proxyPruefung) {
        super.validate(proxyPruefung);
        if (javascriptWaitCallback != null) {
            Assertions.assertThat(javascriptEnabled)
                    .as("When %s is to be used, JavaScript needs to be enabled for it!",
                            javascriptWaitCallback.getClass().getSimpleName())
                    .isTrue();
        }
    }

}
