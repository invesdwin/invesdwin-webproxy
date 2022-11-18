package de.invesdwin.webproxy.crawler.sources;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.annotation.concurrent.ThreadSafe;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import de.invesdwin.util.lang.uri.URIs;
import de.invesdwin.webproxy.GetPageConfig;
import de.invesdwin.webproxy.IWebproxyService;

@ThreadSafe
@Named
public class SpeedtestAtCrawlerSource extends AUrisProxyCrawlerSourceTemplate {

    private static final String BASIS_URL = "http://proxy.speedtest.at/";
    private static final String START_URL = BASIS_URL + "proxybyPerformance.php";

    @Inject
    private IWebproxyService webproxy;

    @Override
    protected Set<URI> getUris() throws InterruptedException, ExecutionException {
        final GetPageConfig config = getInternalPageConfig();
        final HtmlPage page = (HtmlPage) webproxy.getPage(config, URIs.asUri(START_URL)).get();
        final Set<URI> proxyLinks = new HashSet<URI>();
        for (final HtmlAnchor l : page.getAnchors()) {
            final String href = l.getHrefAttribute();
            if (href.startsWith("proxybyPerformance.php?offset=")) {
                final String uri = BASIS_URL + href;
                proxyLinks.add(URIs.asUri(uri));
            }
        }
        return proxyLinks;
    }

}