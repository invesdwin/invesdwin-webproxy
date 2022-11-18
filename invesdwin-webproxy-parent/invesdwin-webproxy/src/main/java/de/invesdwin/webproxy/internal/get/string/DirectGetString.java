package de.invesdwin.webproxy.internal.get.string;

import javax.annotation.concurrent.ThreadSafe;
import jakarta.inject.Named;

import de.invesdwin.webproxy.internal.proxypool.DummyProxyPool;

@ThreadSafe
@Named
public class DirectGetString extends AGetString {

    public DirectGetString() {
        super(new DummyProxyPool(), true, false);
    }

}
