package de.invesdwin.webproxy.crawler.sources;

import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import de.invesdwin.context.test.ATest;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.webproxy.broker.contract.schema.RawProxy;

@ThreadSafe
public class MultiProxyOrgCrawlerSourceTest extends ATest {

    @Inject
    private MultiProxyOrgCrawlerSource crawler;

    @Test
    public void test() throws InterruptedException, ExecutionException {
        final Set<RawProxy> proxies = crawler.getRawProxies();
        Assertions.assertThat(proxies.size()).isGreaterThan(0);
        log.info("Crawled: " + proxies.size());
    }

}
