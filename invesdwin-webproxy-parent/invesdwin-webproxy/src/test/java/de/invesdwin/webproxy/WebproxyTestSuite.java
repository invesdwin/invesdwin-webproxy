package de.invesdwin.webproxy;

import javax.annotation.concurrent.Immutable;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.invesdwin.webproxy.callbacks.statistics.basis.FailureStatisticsTest;
import de.invesdwin.webproxy.internal.get.IsNotProxiesFaultProxyResponseCallbackTest;
import de.invesdwin.webproxy.internal.get.page.DirectGetPageTest;
import de.invesdwin.webproxy.internal.get.string.DirectGetStringTest;
import de.invesdwin.webproxy.internal.get.string.ProxyPoolGetStringTest;

@RunWith(Suite.class)
@SuiteClasses({ FailureStatisticsTest.class, DirectGetPageTest.class, DirectGetStringTest.class,
        IsNotProxiesFaultProxyResponseCallbackTest.class, WebproxyServiceTest.class, ProxyPoolGetStringTest.class })
@Immutable
public class WebproxyTestSuite {

}
