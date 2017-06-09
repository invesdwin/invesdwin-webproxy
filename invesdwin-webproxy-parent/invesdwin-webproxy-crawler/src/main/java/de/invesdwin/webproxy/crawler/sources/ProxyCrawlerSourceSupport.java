package de.invesdwin.webproxy.crawler.sources;

import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.annotation.concurrent.Immutable;
import javax.inject.Named;

import de.invesdwin.webproxy.broker.contract.schema.RawProxy;

@Immutable
@Named
public class ProxyCrawlerSourceSupport implements IProxyCrawlerSource {

    @Override
    public Set<RawProxy> getRawProxies() throws InterruptedException, ExecutionException {
        return null;
    }

}
