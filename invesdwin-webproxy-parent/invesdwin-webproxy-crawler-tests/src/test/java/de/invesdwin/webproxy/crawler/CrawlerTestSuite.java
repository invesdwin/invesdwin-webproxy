package de.invesdwin.webproxy.crawler;

import javax.annotation.concurrent.Immutable;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import de.invesdwin.webproxy.crawler.sources.FreeProxyListsComCrawlerSourceTest;
import de.invesdwin.webproxy.crawler.sources.HideMyAssComCrawlerSourceTest;
import de.invesdwin.webproxy.crawler.sources.MultiProxyOrgCrawlerSourceTest;
import de.invesdwin.webproxy.crawler.sources.MyProxyComCrawlerSourceTest;
import de.invesdwin.webproxy.crawler.sources.ProxyListsNetCrawlerSourceTest;
import de.invesdwin.webproxy.crawler.sources.SpeedtestAtCrawlerSourceTest;

@Suite
@SelectClasses({ FreeProxyListsComCrawlerSourceTest.class, HideMyAssComCrawlerSourceTest.class,
        MultiProxyOrgCrawlerSourceTest.class, MyProxyComCrawlerSourceTest.class, ProxyListsNetCrawlerSourceTest.class,
        SpeedtestAtCrawlerSourceTest.class })
@Immutable
public class CrawlerTestSuite {

}
