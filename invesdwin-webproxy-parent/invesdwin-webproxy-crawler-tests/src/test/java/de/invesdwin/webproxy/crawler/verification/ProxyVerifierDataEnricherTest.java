package de.invesdwin.webproxy.crawler.verification;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

import org.junit.Test;

import de.invesdwin.context.integration.ws.registry.RegistryServiceStub;
import de.invesdwin.context.test.ATest;
import de.invesdwin.context.test.TestContext;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.webproxy.broker.contract.ProxyUtil;
import de.invesdwin.webproxy.broker.contract.schema.Proxy;
import de.invesdwin.webproxy.broker.contract.schema.RawProxy;

@ThreadSafe
public class ProxyVerifierDataEnricherTest extends ATest {

    @Inject
    private ProxyVerifierDataEnricher pruefer;

    @Override
    public void setUpContext(final TestContext ctx) throws Exception {
        super.setUpContext(ctx);
        ctx.deactivate(RegistryServiceStub.class);
    }

    @Test
    public void testVerification() throws InterruptedException {
        final RawProxy rawProxy = ProxyUtil.valueOf("93.167.245.178", 9100);
        final Proxy proxy = pruefer.enrich(rawProxy);
        Assertions.assertThat(proxy).isNotNull();
        log.info(proxy.toString());
    }
}
