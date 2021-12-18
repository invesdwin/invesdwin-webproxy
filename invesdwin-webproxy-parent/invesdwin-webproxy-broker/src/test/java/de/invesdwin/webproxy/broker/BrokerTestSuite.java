package de.invesdwin.webproxy.broker;

import javax.annotation.concurrent.Immutable;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import de.invesdwin.webproxy.broker.internal.BrokerServiceTest;
import de.invesdwin.webproxy.broker.internal.persistence.ProxyDaoTest;

@Suite
@SelectClasses({ ProxyDaoTest.class, BrokerServiceTest.class, ServerTest.class })
@Immutable
public class BrokerTestSuite {

}
