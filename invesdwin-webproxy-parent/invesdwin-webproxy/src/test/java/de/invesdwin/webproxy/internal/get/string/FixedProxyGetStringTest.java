package de.invesdwin.webproxy.internal.get.string;

import java.net.URI;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

import javax.annotation.concurrent.ThreadSafe;

import org.junit.jupiter.api.Test;

import de.invesdwin.context.test.ATest;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.lang.uri.URIs;
import de.invesdwin.webproxy.GetStringConfig;
import de.invesdwin.webproxy.broker.contract.ProxyUtil;
import de.invesdwin.webproxy.broker.contract.schema.Proxy;
import de.invesdwin.webproxy.broker.contract.schema.ProxyQuality;
import de.invesdwin.webproxy.broker.contract.schema.ProxyType;

@ThreadSafe
public class FixedProxyGetStringTest extends ATest {

    private final URI uri = URIs.asUri("https://subes.dyndns.org/index.php");

    @Test
    public void testCodeenBot() throws InterruptedException, ExecutionException {
        final Proxy proxy = ProxyUtil.valueOf("213.217.58.18", 1080, ProxyType.SOCKS, ProxyQuality.TRANSPARENT,
                Locale.getDefault().getCountry(), TimeZone.getDefault().getID());
        final GetStringConfig config = new GetStringConfig();
        final FixedProxyGetString get = new FixedProxyGetString(proxy, false);
        final FixedProxyGetString get2 = new FixedProxyGetString(proxy, true);

        String res = get.get(config, uri).get();
        log.info("1:" + (res == null ? "null" : res.toString()));
        Assertions.assertThat(res).isNotNull();

        res = get2.get(config, uri).get();
        log.info("2:" + (res == null ? "null" : res.toString()));
        Assertions.assertThat(res).isNotNull();
        Assertions.assertThat(res).contains("subes private fileserver");
    }
}
