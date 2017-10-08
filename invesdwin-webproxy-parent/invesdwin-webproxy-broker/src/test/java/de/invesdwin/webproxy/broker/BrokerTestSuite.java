package de.invesdwin.webproxy.broker;

import javax.annotation.concurrent.Immutable;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

import de.invesdwin.webproxy.broker.internal.BrokerServiceTest;
import de.invesdwin.webproxy.broker.internal.persistence.ProxyDaoTest;

@RunWith(JUnitPlatform.class)
@SelectClasses({ ProxyDaoTest.class, BrokerServiceTest.class, ServerTest.class })
@Immutable
public class BrokerTestSuite {

}
