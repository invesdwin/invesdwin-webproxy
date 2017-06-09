package de.invesdwin.webproxy.internal.get.page;

import java.net.URI;

import javax.annotation.concurrent.ThreadSafe;

import com.gargoylesoftware.htmlunit.Page;

import de.invesdwin.webproxy.GetPageConfig;
import de.invesdwin.webproxy.HtmlPages;
import de.invesdwin.webproxy.broker.contract.schema.Proxy;
import de.invesdwin.webproxy.internal.get.AGet;
import de.invesdwin.webproxy.internal.proxypool.IProxyPool;

@ThreadSafe
public abstract class AGetPage extends AGet<Page, GetPageConfig> {

    public AGetPage(final IProxyPool pool, final boolean retryAllowed, final boolean proxyVerification) {
        super(pool, retryAllowed, proxyVerification);
    }

    @Override
    protected WebClientWorker newWorker(final GetPageConfig config, final URI uri, final Proxy proxy) {
        return new WebClientWorker(config, uri, proxy);
    }

    @Override
    protected String responseToString(final Page response) {
        return HtmlPages.toHtml(response);
    }

}
