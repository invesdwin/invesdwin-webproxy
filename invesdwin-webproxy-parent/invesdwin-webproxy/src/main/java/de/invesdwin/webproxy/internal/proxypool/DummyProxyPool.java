package de.invesdwin.webproxy.internal.proxypool;

import javax.annotation.concurrent.Immutable;

import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.webproxy.callbacks.statistics.basis.SessionStatistics;
import de.invesdwin.webproxy.internal.get.AGetConfig;

@Immutable
public class DummyProxyPool implements IProxyPool {

    @Override
    public PooledProxy getProxy(final AGetConfig config, final SessionStatistics session) throws InterruptedException {
        return null;
    }

    @Override
    public void discardProxy(final AGetConfig config, final SessionStatistics session, final PooledProxy proxy,
            final Throwable reason) {
        Assertions.assertThat(proxy).isNull();
    }

    @Override
    public void returnProxy(final AGetConfig config, final SessionStatistics session, final PooledProxy proxy,
            final boolean downloadSuccesful) {
        Assertions.assertThat(proxy).isNull();
    }

}
