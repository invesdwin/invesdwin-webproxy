package de.invesdwin.webproxy.crawler.sources;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.annotation.concurrent.ThreadSafe;
import jakarta.inject.Named;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import de.invesdwin.util.lang.uri.URIs;
import de.invesdwin.webproxy.GetPageConfig;

@ThreadSafe
@Named
public class MyProxyComCrawlerSource extends AUrisProxyCrawlerSourceTemplate {

    private static final String URL_BASIS = "http://www.my-proxy.com";
    private static final String URL_START = URL_BASIS + "/free-proxy-list.html";
    private static final String HREF_BASIS_RELATIVE = "free-";
    private static final String HREF_BASIS_ABSOLUTE = URL_BASIS + "/" + HREF_BASIS_RELATIVE;

    @Override
    protected Set<URI> getUris() throws InterruptedException, ExecutionException {
        final GetPageConfig config = getInternalPageConfig();
        final HtmlPage page = (HtmlPage) webproxy.getPage(config, URIs.asUri(URL_START)).get();

        final Set<URI> proxyLinks = new HashSet<URI>();
        for (final HtmlAnchor l : page.getAnchors()) {
            final String href = l.getHrefAttribute();
            if (href.startsWith(HREF_BASIS_ABSOLUTE)) {
                proxyLinks.add(URIs.asUri(href));
            } else if (href.startsWith(HREF_BASIS_RELATIVE)) {
                proxyLinks.add(URIs.asUri(URL_BASIS + "/" + href));
            }
        }

        return proxyLinks;
    }
}
