package de.invesdwin.webproxy.broker;

import javax.annotation.concurrent.Immutable;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.invesdwin.webproxy.broker.internal.BrokerServiceTest;
import de.invesdwin.webproxy.broker.internal.persistence.ProxyDaoTest;

@RunWith(Suite.class)
@SuiteClasses({ ProxyDaoTest.class, BrokerServiceTest.class, ServerTest.class })
@Immutable
public class BrokerTestSuite {

}
