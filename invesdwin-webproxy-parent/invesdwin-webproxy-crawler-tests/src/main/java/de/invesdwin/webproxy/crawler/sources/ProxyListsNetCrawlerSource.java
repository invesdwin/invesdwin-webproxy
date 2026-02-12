package de.invesdwin.webproxy.crawler.sources;

import java.net.URI;
import java.util.Set;

import javax.annotation.concurrent.ThreadSafe;

import de.invesdwin.util.collections.factory.ILockCollectionFactory;
import de.invesdwin.util.lang.uri.URIs;
import jakarta.inject.Named;

@ThreadSafe
@Named
public class ProxyListsNetCrawlerSource extends AUrisProxyCrawlerSourceTemplate {

    private static final String BASIS_URL = "http://www.proxylists.net";

    @Override
    protected Set<URI> getUris() {
        final Set<URI> uris = ILockCollectionFactory.getInstance(false).newSet();
        for (final TxtList list : TxtList.values()) {
            uris.add(list.getUri());
        }
        return uris;
    }

    private enum TxtList {
        HTTP_HIGHANON("http_highanon"),
        HTTP("http"),
        SOCKS4("socks4"),
        SOCKS5("socks5");

        private final URI uri;

        TxtList(final String txt) {
            this.uri = URIs.asUri(BASIS_URL + "/" + txt + ".txt");
        }

        public URI getUri() {
            return uri;
        }
    }

}
