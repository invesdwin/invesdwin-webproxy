package de.invesdwin.webproxy.internal.get.page;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Named;

import de.invesdwin.webproxy.internal.proxypool.BrokerProxyPool;

@Named
@ThreadSafe
public class ProxyPoolGetPage extends AGetPage {

    public ProxyPoolGetPage() {
        super(new BrokerProxyPool(), true, false);
    }

}
