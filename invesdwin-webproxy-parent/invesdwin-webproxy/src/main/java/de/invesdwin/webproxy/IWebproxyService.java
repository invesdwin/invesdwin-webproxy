package de.invesdwin.webproxy;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;

import de.invesdwin.webproxy.broker.contract.schema.Proxy;

/**
 * This class allows downloading content from the websites.
 * 
 * When proxies are being used, best efforts are being tried to prevent wrong results that proxies send themselves. Even
 * with that you can't count on this working 100% always. Wrong results should be checked from outside and a retry
 * should be done in case that happens. This can be configured into the download directly.
 * 
 * Failed downloads are marked as null elements in the result. The result array is always of the same length as the uri
 * array.
 * 
 * @author subes
 * 
 */
public interface IWebproxyService {

    Future<String> getString(GetStringConfig config, URI uri) throws InterruptedException;

    List<Future<String>> getString(GetStringConfig config, Collection<URI> uris) throws InterruptedException;

    <T extends Page> Future<T> getPage(GetPageConfig config, URI uri) throws InterruptedException;

    <T extends Page> List<Future<T>> getPage(GetPageConfig config, Collection<URI> uris) throws InterruptedException;

    /**
     * Returns any proxy that is not in use. This verifies proxies before returning them. Proxies rotate as soon as the
     * list gets empty internally.
     * 
     * Proxies should not lie around unused for too long, because proxy splash pages might reappear that have been
     * worked around during verification.
     */
    Proxy newProxy(GetStringConfig config) throws InterruptedException;

    /**
     * Returns a WebClient that is already configured. You can use that to download over a fixed proxy with a fixed
     * session.
     * 
     * WARNING: WebClient is not thread safe!
     */
    WebClient newWebClient(GetPageConfig config);

    int getWorkingProxiesCount();

}
