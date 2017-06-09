package de.invesdwin.webproxy.internal.get.string;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Named;

import de.invesdwin.webproxy.internal.proxypool.BrokerProxyPool;

@ThreadSafe
@Named
public class ProxyPoolGetString extends AGetString {

    public ProxyPoolGetString() {
        super(new BrokerProxyPool(), true, false);
    }

}
