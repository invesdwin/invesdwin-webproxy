package de.invesdwin.webproxy.crawler;

import javax.annotation.concurrent.Immutable;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.invesdwin.webproxy.crawler.sources.FreeProxyListsComCrawlerSourceTest;
import de.invesdwin.webproxy.crawler.sources.HideMyAssComCrawlerSourceTest;
import de.invesdwin.webproxy.crawler.sources.MultiProxyOrgCrawlerSourceTest;
import de.invesdwin.webproxy.crawler.sources.MyProxyComCrawlerSourceTest;
import de.invesdwin.webproxy.crawler.sources.ProxyListsNetCrawlerSourceTest;
import de.invesdwin.webproxy.crawler.sources.SpeedtestAtCrawlerSourceTest;

@RunWith(Suite.class)
@SuiteClasses({ FreeProxyListsComCrawlerSourceTest.class, HideMyAssComCrawlerSourceTest.class, MultiProxyOrgCrawlerSourceTest.class,
        MyProxyComCrawlerSourceTest.class, ProxyListsNetCrawlerSourceTest.class, SpeedtestAtCrawlerSourceTest.class })
@Immutable
public class CrawlerTestSuite {

}
