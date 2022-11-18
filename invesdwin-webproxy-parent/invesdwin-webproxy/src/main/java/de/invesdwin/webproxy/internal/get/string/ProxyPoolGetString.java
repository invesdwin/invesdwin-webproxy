package de.invesdwin.webproxy.internal.get.string;

import javax.annotation.concurrent.ThreadSafe;
import jakarta.inject.Named;

import de.invesdwin.webproxy.internal.proxypool.BrokerProxyPool;

@ThreadSafe
@Named
public class ProxyPoolGetString extends AGetString {

    public ProxyPoolGetString() {
        super(new BrokerProxyPool(), true, false);
    }

}
