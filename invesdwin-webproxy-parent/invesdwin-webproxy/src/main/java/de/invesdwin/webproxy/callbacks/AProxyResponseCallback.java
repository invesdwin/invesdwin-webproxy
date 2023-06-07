package de.invesdwin.webproxy.callbacks;

import java.net.URI;

import javax.annotation.concurrent.Immutable;

import com.gargoylesoftware.htmlunit.Page;

import de.invesdwin.util.lang.string.Strings;
import de.invesdwin.util.lang.uri.URIs;
import de.invesdwin.webproxy.HtmlPages;

/**
 * Implements a blacklist as a basis. This checks for failures that may be caused by anti bot secutiy.
 * 
 * @author subes
 * 
 */
@Immutable
public abstract class AProxyResponseCallback {

    /**
     * Decides if a proxy should be dismissed because it gave an invalid response.
     * 
     * originalResponse may be a String, Page or Exception
     */
    public boolean isValidResponse(final URI uri, final String stringResponse, final Object originalResponse) {
        if (Strings.isBlank(stringResponse)) {
            return false;
        }

        final String lowercase = stringResponse.toLowerCase();
        boolean proxyFehler = lowercase.contains("captcha");
        proxyFehler = proxyFehler || lowercase.contains("codeen"); //Codeen Bot Check;
        proxyFehler = proxyFehler || lowercase.contains("malformed request"); //codeen malformed request

        /*
         * We are detecting an unusual number of requests for information from your computer at this time, which will
         * affect the availability of our site for all users.
         */
        proxyFehler = proxyFehler || lowercase.contains("detecting an unusual number of requests");
        /*
         * MSN Money does not support automated routines that repeatedly request information from our site. If you are
         * using such an automated routine, please discontinue.
         */
        proxyFehler = proxyFehler || lowercase.contains("automated routine, please discontinue");
        //An attack was detected from your system...
        proxyFehler = proxyFehler || lowercase.contains("an attack was detected from your system");

        return !proxyFehler;
    }

    /**
     * Decides if another download should be done with another proxy. Useful to do a double check if another proxy gives
     * the same response. With this you can work around evil proxies (e.g. may replace characters) and anti bot
     * security.
     * 
     * The string to be checked should often not be the whole page, but instead only a payload that is needed. Like this
     * timestamps and other things that change every download are not counted for the check.
     */
    public boolean isAnotherDownloadNeededForVerification(final URI uri, final String stringResponse,
            final Object originalResponse) {
        return false;
    }

    public final boolean isValidResponse(final Page page) {
        return isValidResponse(URIs.asUri(page.getUrl()), HtmlPages.toHtml(page), page);
    }
}
