package de.invesdwin.webproxy;

import javax.annotation.concurrent.Immutable;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

import de.invesdwin.webproxy.callbacks.statistics.basis.FailureStatisticsTest;
import de.invesdwin.webproxy.internal.get.IsNotProxiesFaultProxyResponseCallbackTest;
import de.invesdwin.webproxy.internal.get.page.DirectGetPageTest;
import de.invesdwin.webproxy.internal.get.string.DirectGetStringTest;
import de.invesdwin.webproxy.internal.get.string.ProxyPoolGetStringTest;

@RunWith(JUnitPlatform.class)
@SelectClasses({ FailureStatisticsTest.class, DirectGetPageTest.class, DirectGetStringTest.class,
        IsNotProxiesFaultProxyResponseCallbackTest.class, WebproxyServiceTest.class, ProxyPoolGetStringTest.class })
@Immutable
public class WebproxyTestSuite {

}
