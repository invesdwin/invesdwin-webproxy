package de.invesdwin.webproxy.internal.get.page;

import javax.annotation.concurrent.ThreadSafe;

import org.springframework.beans.factory.annotation.Configurable;

import de.invesdwin.webproxy.broker.contract.schema.Proxy;
import de.invesdwin.webproxy.internal.proxypool.FixedProxyPool;

/**
 * This class needs to be reinitialized for each proxy verification. Reuse is impossible.
 * 
 * @author subes
 * 
 */
@ThreadSafe
@Configurable
public class FixedProxyGetPage extends AGetPage {

    public FixedProxyGetPage(final Proxy proxy) {
        super(new FixedProxyPool(proxy, false), false, false);
    }

}
