package de.invesdwin.webproxy.internal.get.page;

import javax.annotation.concurrent.ThreadSafe;
import jakarta.inject.Named;

import de.invesdwin.webproxy.internal.proxypool.DummyProxyPool;

@Named
@ThreadSafe
public class DirectGetPage extends AGetPage {

    public DirectGetPage() {
        super(new DummyProxyPool(), true, false);
    }

}
