package de.invesdwin.webproxy.internal.proxypool;

import java.io.IOException;

import de.invesdwin.webproxy.callbacks.statistics.basis.SessionStatistics;
import de.invesdwin.webproxy.internal.get.AGetConfig;

public interface IProxyPool {

    PooledProxy getProxy(AGetConfig config, SessionStatistics session) throws InterruptedException, IOException;

    void discardProxy(AGetConfig config, SessionStatistics session, PooledProxy proxy, Throwable reason);

    void returnProxy(AGetConfig config, SessionStatistics session, PooledProxy proxy, boolean downloadSuccessful);

}
