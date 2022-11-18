package de.invesdwin.webproxy.crawler.sources;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.annotation.concurrent.ThreadSafe;
import jakarta.inject.Named;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.lang.uri.URIs;
import de.invesdwin.webproxy.GetPageConfig;

@ThreadSafe
@Named
public class HideMyAssComCrawlerSource extends AUrisProxyCrawlerSourceTemplate {

    private static final String URL_BASIS = "http://proxylist.hidemyass.com/";
    private static final String URL_START = URL_BASIS + "1";

    @Override
    protected Set<URI> getUris() throws InterruptedException, ExecutionException {
        final GetPageConfig config = getInternalPageConfig();
        final HtmlPage page = (HtmlPage) webproxy.getPage(config, URIs.asUri(URL_START)).get();

        //First identify the last page, then generate the other index links here
        int lastPage = 0;
        for (final HtmlAnchor l : page.getAnchors()) {
            final String href = l.getHrefAttribute();
            final String linkPath = href.replace("/", "");
            try {
                final int s = Integer.parseInt(linkPath);
                if (s > lastPage) {
                    lastPage = s;
                }
            } catch (final NumberFormatException e) {
                //No number in the link
                continue;
            }
        }
        Assertions.assertThat(lastPage).isNotEqualTo(0);

        final Set<URI> proxyLinks = new HashSet<URI>();
        for (int i = 1; i <= lastPage; i++) {
            proxyLinks.add(URIs.asUri(URL_BASIS + i));
        }
        return proxyLinks;
    }

    @Override
    protected GetPageConfig getInternalPageConfig() {
        return super.getInternalPageConfig().setCssEnabled(true);
    }

    @Override
    protected String getNewlineReplacement() {
        return "";
    }
}