package de.invesdwin.webproxy.internal.get.page;

import java.io.IOException;
import java.net.URI;

import javax.annotation.concurrent.ThreadSafe;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import de.invesdwin.context.log.Log;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.lang.uri.URIs;
import de.invesdwin.util.time.Instant;
import de.invesdwin.util.time.duration.Duration;
import de.invesdwin.webproxy.GetPageConfig;
import de.invesdwin.webproxy.WebClientFactory;
import de.invesdwin.webproxy.broker.contract.schema.Proxy;
import de.invesdwin.webproxy.callbacks.JavascriptWaitCallback;
import de.invesdwin.webproxy.internal.get.ADownloadWorker;

@ThreadSafe
public class WebClientWorker extends ADownloadWorker<Page, GetPageConfig> {

    private final Log log = new Log(this);
    private final WebClient client;

    protected WebClientWorker(final GetPageConfig config, final URI uri, final Proxy proxy) {
        super(config, uri, proxy);
        client = WebClientFactory.initWebClient(config, proxy);
        Assertions.assertThat(client.getOptions().isJavaScriptEnabled())
                .as("JavaScript setting is not the same between client and config!")
                .isEqualTo(config.isJavascriptEnabled());
    }

    @Override
    protected Page download(final GetPageConfig config, final URI uri, final Proxy proxy) throws IOException {
        Page page = null;
        try {
            page = client.getPage(URIs.asUrl(uri));
        } catch (final FailingHttpStatusCodeException e) {
            throw new IOException(e.getMessage(), e);
        } finally {
            //page is null on failure
            delayAutoClose(config, page);
        }
        return page;
    }

    private void delayAutoClose(final GetPageConfig config, final Page page) {
        final JavascriptWaitCallback callback = config.getJavascriptWaitCallback();
        if (page != null && callback != null && page instanceof HtmlPage && config.isJavascriptEnabled()) {
            final Instant start = new Instant();
            boolean timeExpired;
            do {
                timeExpired = new Duration(start).isGreaterThan(callback.getMaxDelay());
                if (timeExpired || callback.isDownloadFinished(client, page)) {
                    if (timeExpired) {
                        log.warn("%s MaxDelay of %s expired on %s", callback.getClass().getName(),
                                callback.getMaxDelay(), page.getWebResponse().getWebRequest().getUrl());
                    }
                    break;
                } else {
                    callback.delay(client);
                }
            } while (!timeExpired);
        }
    }

    @Override
    protected void internalClose() {
        client.close();
    }

}
