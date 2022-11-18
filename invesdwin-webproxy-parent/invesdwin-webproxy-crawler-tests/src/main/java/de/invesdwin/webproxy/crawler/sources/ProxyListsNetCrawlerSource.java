package de.invesdwin.webproxy.crawler.sources;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.concurrent.ThreadSafe;
import jakarta.inject.Named;

import de.invesdwin.util.lang.uri.URIs;

@ThreadSafe
@Named
public class ProxyListsNetCrawlerSource extends AUrisProxyCrawlerSourceTemplate {

    private static final String BASIS_URL = "http://www.proxylists.net";

    @Override
    protected Set<URI> getUris() {
        final Set<URI> uris = new HashSet<URI>();
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
