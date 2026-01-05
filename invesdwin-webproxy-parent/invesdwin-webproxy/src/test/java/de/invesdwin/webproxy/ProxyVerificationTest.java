package de.invesdwin.webproxy;

import java.util.Locale;
import java.util.TimeZone;

import javax.annotation.concurrent.ThreadSafe;

import org.junit.jupiter.api.Test;

import de.invesdwin.context.integration.ws.registry.RegistryServiceStub;
import de.invesdwin.context.test.ATest;
import de.invesdwin.context.test.ITestContextSetup;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.webproxy.broker.contract.BrokerServiceStub;
import de.invesdwin.webproxy.broker.contract.ProxyUtil;
import de.invesdwin.webproxy.broker.contract.schema.Proxy;
import de.invesdwin.webproxy.broker.contract.schema.ProxyQuality;
import de.invesdwin.webproxy.broker.contract.schema.ProxyType;
import jakarta.inject.Inject;

@ThreadSafe
public class ProxyVerificationTest extends ATest {

    @Inject
    private ProxyVerification proxyVeri;

    @Override
    public void setUpContext(final ITestContextSetup ctx) throws Exception {
        super.setUpContext(ctx);
        ctx.deactivateBean(RegistryServiceStub.class);
        ctx.deactivateBean(BrokerServiceStub.class);
    }

    @Test
    public void testVerifyProxy() throws InterruptedException {
        final Proxy proxy = ProxyUtil.valueOf("193.194.69.36", 3128, ProxyType.HTTP, ProxyQuality.TRANSPARENT,
                Locale.getDefault().getCountry(), TimeZone.getDefault().getID());
        final boolean verifyProxy = proxyVeri.verifyProxy(proxy, false, ProxyQuality.TRANSPARENT);
        log.info("%s", proxy.getQuality());
        Assertions.assertThat(verifyProxy).isNotNull();
    }

    @Test
    public void testIsOfMinProxyQuality() {
        final Proxy proxy = new Proxy();
        proxy.setQuality(ProxyQuality.INVISIBLE);
        Assertions.assertThat(proxyVeri.isOfMinProxyQuality(proxy, ProxyQuality.TRANSPARENT)).isTrue();
    }
}
