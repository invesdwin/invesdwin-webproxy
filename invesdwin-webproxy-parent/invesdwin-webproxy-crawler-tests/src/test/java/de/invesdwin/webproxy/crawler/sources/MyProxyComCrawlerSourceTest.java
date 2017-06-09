package de.invesdwin.webproxy.crawler.sources;

import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

import org.junit.Test;

import de.invesdwin.context.test.ATest;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.webproxy.broker.contract.schema.RawProxy;

@ThreadSafe
public class MyProxyComCrawlerSourceTest extends ATest {

    @Inject
    private MyProxyComCrawlerSource crawler;

    //    @Override
    //    public void setUpContext(final TestContext ctx) throws Exception {
    //        ctx.deactivate(RegistryServiceMock.class);
    //        ctx.deactivate(BrokerServiceMock.class);
    //        ctx.deactivate(PortscanServiceMock.class);
    //    }

    @Test
    public void test() throws InterruptedException, ExecutionException {
        final Set<RawProxy> proxies = crawler.getRawProxies();
        Assertions.assertThat(proxies.size()).isGreaterThanOrEqualTo(0);
        log.info("Crawled: " + proxies.size());
    }

}
