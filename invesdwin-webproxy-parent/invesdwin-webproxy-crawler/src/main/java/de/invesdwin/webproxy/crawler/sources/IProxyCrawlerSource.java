package de.invesdwin.webproxy.crawler.sources;

import java.util.Set;
import java.util.concurrent.ExecutionException;

import de.invesdwin.webproxy.broker.contract.schema.RawProxy;

/**
 * A source for proxies. Implementations should follow the following rules:
 * 
 * <ol>
 * <li>The implementation must have the @Named annotation.</li>
 * <li>The crawling must not happen in the constructor.</li>
 * <li>The collected proxies must not be verified in the crawler, this happens later.</li>
 * <li>It must be possible to call the method multiple times in the same instance.</li>
 * </ol>
 * 
 * @author subes
 * 
 */
public interface IProxyCrawlerSource {

    Set<RawProxy> getRawProxies() throws InterruptedException, ExecutionException;

}
