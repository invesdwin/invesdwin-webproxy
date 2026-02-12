package de.invesdwin.webproxy.crawler.sources;

import java.net.URI;
import java.util.Set;

import javax.annotation.concurrent.ThreadSafe;

import de.invesdwin.util.collections.factory.ILockCollectionFactory;

@ThreadSafe
public abstract class AUriProxyCrawlerSourceTemplate extends AUrisProxyCrawlerSourceTemplate {

    @Override
    protected Set<URI> getUris() {
        final Set<URI> uriAsSet = ILockCollectionFactory.getInstance(false).newSet();
        uriAsSet.add(getUri());
        return uriAsSet;
    }

    protected abstract URI getUri();

}
