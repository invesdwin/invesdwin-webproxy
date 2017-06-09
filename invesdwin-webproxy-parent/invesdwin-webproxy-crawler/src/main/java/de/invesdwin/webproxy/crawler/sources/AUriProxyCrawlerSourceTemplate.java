package de.invesdwin.webproxy.crawler.sources;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public abstract class AUriProxyCrawlerSourceTemplate extends AUrisProxyCrawlerSourceTemplate {

    @Override
    protected Set<URI> getUris() {
        final Set<URI> uriAsSet = new HashSet<URI>();
        uriAsSet.add(getUri());
        return uriAsSet;
    }

    protected abstract URI getUri();

}
