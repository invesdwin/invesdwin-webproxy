package de.invesdwin.webproxy.broker.internal;

import java.util.Locale;
import java.util.TimeZone;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

import org.junit.Test;

import de.invesdwin.context.integration.retry.RetryLaterException;
import de.invesdwin.context.persistence.jpa.api.query.QueryConfig;
import de.invesdwin.context.persistence.jpa.test.APersistenceTest;
import de.invesdwin.context.test.TestContext;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.webproxy.broker.contract.BrokerContractProperties;
import de.invesdwin.webproxy.broker.contract.BrokerServiceStub;
import de.invesdwin.webproxy.broker.contract.ProxyUtil;
import de.invesdwin.webproxy.broker.contract.schema.BrokerRequest.AddToBeVerifiedProxiesRequest;
import de.invesdwin.webproxy.broker.contract.schema.BrokerRequest.ProcessResultFromCrawlerRequest;
import de.invesdwin.webproxy.broker.contract.schema.BrokerResponse.GetTaskForCrawlerResponse;
import de.invesdwin.webproxy.broker.contract.schema.BrokerResponse.GetWorkingProxiesResponse;
import de.invesdwin.webproxy.broker.contract.schema.Proxy;
import de.invesdwin.webproxy.broker.contract.schema.ProxyQuality;
import de.invesdwin.webproxy.broker.contract.schema.ProxyType;
import de.invesdwin.webproxy.broker.contract.schema.RawProxy;
import de.invesdwin.webproxy.broker.internal.persistence.RawProxyDao;
import de.invesdwin.webproxy.broker.internal.persistence.RawProxyEntity;

@ThreadSafe
public class BrokerServiceTest extends APersistenceTest {

    @Inject
    private BrokerService broker;
    @Inject
    private BrokerServiceCrawlerTaskHelper crawlerTaskHelper;
    @Inject
    private RawProxyDao rawProxyDao;

    @Override
    public void setUpContext(final TestContext ctx) throws Exception {
        super.setUpContext(ctx);
        ctx.deactivateBean(BrokerServiceStub.class);
    }

    @Override
    public void setUpOnce() throws Exception {
        super.setUpOnce();
        new BrokerServiceTestPreparer().prepare();
        crawlerTaskHelper.triggerProxyCrawling();
    }

    @Test
    public void testGetWorkingProxies() throws RetryLaterException {
        final GetWorkingProxiesResponse response = broker.getWorkingProxies();
        Assertions.assertThat(response.getWorkingProxies().size()).isEqualTo(BrokerServiceTestPreparer.COUNT_PROXIES);
    }

    @Test
    public void testAddToBeVerifiedProxies() {
        final AddToBeVerifiedProxiesRequest request = new AddToBeVerifiedProxiesRequest();
        request.getToBeVerifiedProxies().add(ProxyUtil.valueOf("127.0.0.1", 1));
        broker.addToBeVerifiedProxies(request);
    }

    @Test
    public void testGetTaskForCrawler() {
        GetTaskForCrawlerResponse response = broker.getTaskForCrawler();
        Assertions.assertThat(response.isCrawlForProxies()).isTrue();
        Assertions.assertThat(response.getToBeVerifiedProxies().size()).isZero();
        Assertions.assertThat(response.getToBeScannedPorts().size()).isGreaterThan(0);
        response = broker.getTaskForCrawler();
        Assertions.assertThat(response.getToBeVerifiedProxies().size())
                .isEqualTo(BrokerContractProperties.MAX_PROXIES_PER_TASK);
        Assertions.assertThat(response.getToBeScannedPorts().size())
                .isEqualTo(BrokerProperties.MAX_SPECIFIC_TO_BE_SCANNED_PORTS + BrokerProperties
                        .calculateAdditionalRandomToBeScannedPorts(BrokerProperties.MAX_SPECIFIC_TO_BE_SCANNED_PORTS));
    }

    @Test
    public void testProcessResultFromCrawler() {
        final ProcessResultFromCrawlerRequest request = new ProcessResultFromCrawlerRequest();
        for (final RawProxyEntity rawProxyEnt : rawProxyDao
                .findAll(new QueryConfig().setMaxResults(BrokerContractProperties.MAX_PROXIES_PER_TASK / 2))) {
            final RawProxy rawProxy = rawProxyEnt.toRawProxy();
            final Proxy proxy = ProxyUtil.valueOf(rawProxy, ProxyType.HTTP, ProxyQuality.TRANSPARENT,
                    Locale.getDefault().getCountry(), TimeZone.getDefault().getID());
            request.getSuccessfullyVerifiedProxies().add(proxy);
        }
        for (final RawProxyEntity rawProxyEnt : rawProxyDao
                .findAll(new QueryConfig().setMaxResults(BrokerContractProperties.MAX_PROXIES_PER_TASK / 2))) {
            request.getUnsuccessfullyVerifiedProxies().add(rawProxyEnt.toRawProxy());
        }
        broker.processResultFromCrawler(request);

        //reset
        clearAllTables();
        new BrokerServiceTestPreparer().prepare();
    }
}
