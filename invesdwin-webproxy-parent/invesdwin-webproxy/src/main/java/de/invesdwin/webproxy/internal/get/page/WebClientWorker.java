package de.invesdwin.webproxy.internal.get.page;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;

import javax.annotation.concurrent.ThreadSafe;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomChangeListener;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import de.invesdwin.context.log.Log;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.lang.reflection.Reflections;
import de.invesdwin.util.lang.uri.URIs;
import de.invesdwin.util.time.Instant;
import de.invesdwin.webproxy.GetPageConfig;
import de.invesdwin.webproxy.WebClientFactory;
import de.invesdwin.webproxy.broker.contract.schema.Proxy;
import de.invesdwin.webproxy.callbacks.JavascriptWaitCallback;
import de.invesdwin.webproxy.internal.get.ADownloadWorker;

@ThreadSafe
public class WebClientWorker extends ADownloadWorker<Page, GetPageConfig> {

    private static final int MAX_DOM_LISTENERS = 100_000;
    private static final MethodHandle DOM_LISTENERS_GETTER;

    static {
        final Method safeGetDomListenersMethod = Reflections.findMethod(DomNode.class, "safeGetDomListeners");
        Reflections.makeAccessible(safeGetDomListenersMethod);
        try {
            DOM_LISTENERS_GETTER = MethodHandles.lookup().unreflect(safeGetDomListenersMethod);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

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
                timeExpired = start.isGreaterThan(callback.getMaxDelay());
                if (timeExpired || callback.isDownloadFinished(client, page)
                        || abortOnTooManyDomListeners(callback, page)) {
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

    private boolean abortOnTooManyDomListeners(final JavascriptWaitCallback callback, final Page page) {
        if (page instanceof DomNode) {
            final DomNode cPage = (DomNode) page;
            try {
                final List<DomChangeListener> domListeners = (List<DomChangeListener>) DOM_LISTENERS_GETTER
                        .invoke(cPage);
                if (domListeners.size() > MAX_DOM_LISTENERS) {
                    log.warn(
                            "%s max dom listeners [%s] exceeded on %s [%s]. Aborting because otherwise a memory overflow will happen.",
                            callback.getClass().getName(), MAX_DOM_LISTENERS,
                            page.getWebResponse().getWebRequest().getUrl(), domListeners.size());
                    return true;
                }
            } catch (final Throwable e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    @Override
    protected void internalClose() {
        client.close();
    }

}
