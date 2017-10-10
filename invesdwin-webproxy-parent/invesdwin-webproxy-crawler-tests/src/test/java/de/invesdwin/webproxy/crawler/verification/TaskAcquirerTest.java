package de.invesdwin.webproxy.crawler.verification;

import java.util.concurrent.TimeUnit;

import javax.annotation.concurrent.ThreadSafe;

import org.junit.Test;

import de.invesdwin.context.integration.ws.registry.RegistryServiceStub;
import de.invesdwin.context.test.ATest;
import de.invesdwin.context.test.TestContext;
import de.invesdwin.webproxy.crawler.sources.IProxyCrawlerSource;
import de.invesdwin.webproxy.crawler.sources.SpeedtestAtCrawlerSource;
import de.invesdwin.webproxy.portscan.contract.IPortscanClient;

@ThreadSafe
public class TaskAcquirerTest extends ATest {

    @Override
    public void setUpContext(final TestContext ctx) throws Exception {
        super.setUpContext(ctx);
        ctx.replace(IPortscanClient.class, TaskAcquirerCache.class);
        ctx.deactivate(RegistryServiceStub.class);
        ctx.replace(IProxyCrawlerSource.class, SpeedtestAtCrawlerSource.class);
        ctx.deactivate(TaskAcquirerStub.class);
    }

    @Test
    public void testCrawlAll() throws InterruptedException {
        TimeUnit.DAYS.sleep(Long.MAX_VALUE);
    }

}
