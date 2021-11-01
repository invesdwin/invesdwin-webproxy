package de.invesdwin.webproxy;

import javax.annotation.concurrent.NotThreadSafe;

import com.gargoylesoftware.htmlunit.BrowserVersion;

import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.time.duration.Duration;
import de.invesdwin.webproxy.broker.contract.schema.Proxy;
import de.invesdwin.webproxy.broker.contract.schema.ProxyQuality;
import de.invesdwin.webproxy.callbacks.AProxyResponseCallback;
import de.invesdwin.webproxy.callbacks.JavascriptWaitCallback;
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

    public GetPageConfig setJavascriptWaitCallback(final JavascriptWaitCallback javascriptWaitCallback) {
        this.javascriptWaitCallback = javascriptWaitCallback;
        return this;
    }

    public boolean isJavascriptEnabled() {
        return javascriptEnabled;
    }

    public GetPageConfig setJavascriptEnabled(final boolean javascriptEnabled) {
        this.javascriptEnabled = javascriptEnabled;
        return this;
    }

    public int getMaxPageRefreshCount() {
        return maxPageRefreshCount;
    }

    public GetPageConfig setMaxPageRefreshCount(final int maxPageRefreshCount) {
        Assertions.assertThat(maxPageRefreshCount).isGreaterThanOrEqualTo(0);
        this.maxPageRefreshCount = maxPageRefreshCount;
        return this;
    }

    public GetPageConfig setCssEnabled(final boolean cssEnabled) {
        this.cssEnabled = cssEnabled;
        return this;
    }

    public boolean isCssEnabled() {
        return cssEnabled;
    }

    /**************** overrides ******************/

    @Override
    public GetPageConfig setMaxParallelDownloads(final Integer maxParallelDownloads) {
        super.setMaxParallelDownloads(maxParallelDownloads);
        return this;
    }

    @Override
    public GetPageConfig setProxyResponseCallback(final AProxyResponseCallback proxyResponseCallback) {
        super.setProxyResponseCallback(proxyResponseCallback);
        return this;
    }

    @Override
    public GetPageConfig setBrowserVersion(final BrowserVersion browserVersion) {
        super.setBrowserVersion(browserVersion);
        return this;
    }

    @Override
    public GetPageConfig setStatisticsCallback(final AStatisticsCallback statisticsCallback) {
        super.setStatisticsCallback(statisticsCallback);
        return this;
    }

    @Override
    public GetPageConfig resetVisitedUris() {
        super.resetVisitedUris();
        return this;
    }

    @Override
    public GetPageConfig setFilterVisitedUris(final boolean filterVisitedUris) {
        super.setFilterVisitedUris(filterVisitedUris);
        return this;
    }

    @Override
    public GetPageConfig setUseProxyPool(final boolean useProxy) {
        super.setUseProxyPool(useProxy);
        return this;
    }

    @Override
    public GetPageConfig setFixedProxy(final Proxy fixedProxy) {
        super.setFixedProxy(fixedProxy);
        return this;
    }

    @Override
    public GetPageConfig setSystemProxyAsFixedProxy() {
        super.setSystemProxyAsFixedProxy();
        return this;
    }

    @Override
    public GetPageConfig setMinProxyQuality(final ProxyQuality minProxyQuality) {
        super.setMinProxyQuality(minProxyQuality);
        return this;
    }

    @Override
    public GetPageConfig setMaxDownloadTryDuration(final Duration maxDownloadTryDuration) {
        super.setMaxDownloadTryDuration(maxDownloadTryDuration);
        return this;
    }

    @Override
    public GetPageConfig setMaxDownloadRetries(final int maxDownloadRetries) {
        super.setMaxDownloadRetries(maxDownloadRetries);
        return this;
    }

    @Override
    public GetPageConfig setMaxDownloadRetriesWarningOnly(final boolean maxDownloadRetriesWarningOnly) {
        super.setMaxDownloadRetriesWarningOnly(maxDownloadRetriesWarningOnly);
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
