package de.invesdwin.webproxy.crawler.sources;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

import com.gargoylesoftware.htmlunit.Page;

import de.invesdwin.util.concurrent.future.Futures;
import de.invesdwin.webproxy.GetPageConfig;
import de.invesdwin.webproxy.IWebproxyService;
import de.invesdwin.webproxy.broker.contract.schema.RawProxy;
import de.invesdwin.webproxy.crawler.CrawlerProperties;

@ThreadSafe
public abstract class AUrisProxyCrawlerSourceTemplate extends AProxyCrawlerSource {

    @Inject
    protected IWebproxyService webproxy;

    @Override
    public Set<RawProxy> getRawProxies() throws InterruptedException, ExecutionException {
        final List<Page> pages = Futures.get(webproxy.getPage(getInternalPageConfig(), getUris()));
        final Set<RawProxy> proxies = new HashSet<RawProxy>();
        for (final Page page : pages) {
            proxies.addAll(extractRawProxies(page));
        }
        return proxies;
    }

    protected abstract Set<URI> getUris() throws InterruptedException, ExecutionException;

    protected GetPageConfig getInternalPageConfig() {
        final GetPageConfig config = new GetPageConfig();
        final int workingProxiesCount = webproxy.getWorkingProxiesCount();
        if (workingProxiesCount >= CrawlerProperties.CRAWL_WITH_PROXIES_THRESHOLD) {
            config.setUseProxyPool(true);
        }
        return config;
    }

}
