package de.invesdwin.webproxy.crawler.sources;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import javax.annotation.concurrent.ThreadSafe;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import de.invesdwin.context.integration.csv.CsvVerification;
import de.invesdwin.util.lang.string.Strings;
import de.invesdwin.util.lang.uri.URIs;
import de.invesdwin.webproxy.GetPageConfig;
import de.invesdwin.webproxy.HtmlPages;
import de.invesdwin.webproxy.IWebproxyService;
import de.invesdwin.webproxy.callbacks.JavascriptWaitCallback;

@ThreadSafe
@Named
public class FreeProxyListsComCrawlerSource extends AUrisProxyCrawlerSourceTemplate {

    private static final CsvVerification CSV_VERIFICATION_4 = new CsvVerification(4, "\t");
    private static final CsvVerification CSV_VERIFICATION_2 = new CsvVerification(2, "\t");
    private static final String URL_START = "http://www.freeproxylists.com";

    private static final JavascriptWaitCallback AJAX_DELAY_CALLBACK = new JavascriptWaitCallback() {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isDownloadFinished(final WebClient client, final Page page) {
            final HtmlPage p = (HtmlPage) page;
            String table = HtmlPages.extractTableRows(p, CSV_VERIFICATION_2);
            if (Strings.isBlank(table)) {
                table = HtmlPages.extractTableRows(p, CSV_VERIFICATION_4);
            }
            return !Strings.isBlank(table);
        }

    };

    @Inject
    private IWebproxyService webproxy;

    @Override
    protected GetPageConfig getInternalPageConfig() {
        //Zu viele parallele Anfragen verursachen massig Last, daher begrenzt
        return super.getInternalPageConfig().setJavascriptEnabled(true)
                .setJavascriptWaitCallback(AJAX_DELAY_CALLBACK)
                .setMaxParallelDownloads(10);
    }

    @Override
    protected Set<URI> getUris() throws InterruptedException, ExecutionException {
        final GetPageConfig config = getInternalPageConfig();
        final Set<URI> proxyUris = new HashSet<URI>();
        for (final SubPage subPage : SubPage.values()) {
            final HtmlPage page = (HtmlPage) webproxy.getPage(config, subPage.getIndex()).get();
            for (final HtmlAnchor l : page.getAnchors()) {
                final String href = l.getHrefAttribute();
                if (subPage.isProxyLink(href)) {
                    final URI uri = URIs.asUri(URL_START + "/" + href);
                    proxyUris.add(uri);
                }
            }
        }
        return proxyUris;
    }

    private enum SubPage {
        //The country specific pages just contain filtered proxies from other pages
        //US("us"),
        //UK("uk"),
        //CA("ca"),
        //FR("fr"),
        ELITE("elite"),
        ANONYMOUS("anonymous", "anon"),
        NON_ANONYMOUS("non-anonymous", "nonanon"),
        HTTPS("https"),
        SOCKS("socks"),
        STANDARD("standard");

        private final URI index;
        private final Pattern proxyLinkPattern;

        SubPage(final String partOfBoth) {
            this(partOfBoth, partOfBoth);
        }

        SubPage(final String indexDescription, final String proxyLinkDescription) {
            index = URIs.asUri(URL_START + "/" + indexDescription + ".html");
            proxyLinkPattern = Pattern.compile(proxyLinkDescription + "/[0-9]{10}.html");
        }

        public URI getIndex() {
            return index;
        }

        public boolean isProxyLink(final String uri) {
            return proxyLinkPattern.matcher(uri).matches();
        }
    }

}
