package de.invesdwin.webproxy;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

import org.junit.Test;

import de.invesdwin.context.integration.IntegrationProperties;
import de.invesdwin.context.integration.network.NetworkUtil;
import de.invesdwin.context.integration.retry.RetryLaterException;
import de.invesdwin.context.integration.ws.registry.RegistryServiceStub;
import de.invesdwin.context.log.error.Err;
import de.invesdwin.context.test.ATest;
import de.invesdwin.context.test.TestContext;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.concurrent.Executors;
import de.invesdwin.util.concurrent.WrappedExecutorService;
import de.invesdwin.util.lang.uri.Addresses;
import de.invesdwin.webproxy.broker.contract.BrokerServiceStub;
import de.invesdwin.webproxy.broker.contract.IBrokerService;
import de.invesdwin.webproxy.broker.contract.schema.BrokerResponse.GetWorkingProxiesResponse;
import de.invesdwin.webproxy.broker.contract.schema.Proxy;
import de.invesdwin.webproxy.callbacks.statistics.ConsoleReportStatisticsCallback;

@ThreadSafe
public class ProxiesProductiveDataTest extends ATest {

    private final WrappedExecutorService executor = Executors.newFixedThreadPool(
            ProxiesProductiveDataTest.class.getSimpleName(), WebproxyProperties.MAX_PARALLEL_DOWNLOADS);
    private final AtomicInteger workingProxies = new AtomicInteger();
    private final AtomicInteger notWorkingProxies = new AtomicInteger();

    @Inject
    private IBrokerService broker;
    @Inject
    private IWebproxyService webproxy;
    @Inject
    private ProxyVerification proxyVeri;

    @Override
    public void setUpContext(final TestContext ctx) throws Exception {
        super.setUpContext(ctx);
        ctx.deactivateBean(RegistryServiceStub.class);
        ctx.deactivateBean(BrokerServiceStub.class);
    }

    @Test
    public void testWorkingProxies() throws InterruptedException, RetryLaterException {
        final ConsoleReportStatisticsCallback callback = new ConsoleReportStatisticsCallback();
        final GetWorkingProxiesResponse response = broker.getWorkingProxies();
        for (final Proxy p : response.getWorkingProxies()) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    final GetStringConfig c = new GetStringConfig();
                    c.withFixedProxy(p);
                    c.withStatisticsCallback(callback);
                    String ret;
                    try {
                        ret = webproxy.getString(c, IntegrationProperties.INTERNET_CHECK_URIS.get(0)).get();
                    } catch (final InterruptedException e) {
                        ret = null;
                    } catch (final ExecutionException e) {
                        //Err.process(e);
                        ret = null;
                    }
                    if (ret == null) {
                        notWorkingProxies.incrementAndGet();
                    } else {
                        if (NetworkUtil.getExternalAddress().getHostAddress().equals(ret.trim())
                                || !Addresses.isIp(ret.trim())) {
                            //log.warn("Invalid response from %s: %s", p, ret);
                            notWorkingProxies.incrementAndGet();
                        } else {
                            workingProxies.incrementAndGet();
                            log.info("Proxy works: " + p);
                        }
                    }
                }
            });
        }
        new Thread() {
            @Override
            public void run() {
                while (!executor.isTerminated()) {
                    try {
                        TimeUnit.SECONDS.sleep(5);
                    } catch (final InterruptedException e) {
                        throw Err.process(e);
                    }
                    proxyVeri.getStatisticsCallback().logFinalReport();
                    log.info("%s% done. Of %s/%s proxies %s have worked and %s have not worked!",
                            (int) (((workingProxies.get() + notWorkingProxies.get())
                                    / (double) response.getWorkingProxies().size()) * 100),
                            workingProxies.get() + notWorkingProxies.get(), response.getWorkingProxies().size(),
                            workingProxies.get(), notWorkingProxies.get());
                }
            };
        }.start();
        executor.awaitPendingCount(0);
        executor.shutdown();
        callback.logFinalReport();
        Assertions.assertThat(notWorkingProxies.get()).isZero();
    }
}
