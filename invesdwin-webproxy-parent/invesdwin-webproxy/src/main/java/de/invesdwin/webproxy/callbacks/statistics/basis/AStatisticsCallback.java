package de.invesdwin.webproxy.callbacks.statistics.basis;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public abstract class AStatisticsCallback {

    @GuardedBy("this")
    protected SessionStatistics allSessions = new SessionStatistics();

    public void downloadSuccessful(final SessionStatistics session, final DownloadStatistics statistics) {
        session.addDownloadStatistics(statistics, null);
    }

    public void downloadFailure(final SessionStatistics session, final DownloadStatistics statistics,
            final Throwable reason) {
        session.addDownloadStatistics(statistics, reason);
    }

    public void proxyNotWorkingAnymore(final SessionStatistics session, final ProxyStatistics statistics) {
        session.addProxyStatistics(statistics);
    }

    public void proxyStillWorks(final SessionStatistics session, final ProxyStatistics statistics) {
        session.addProxyStatistics(statistics);
    }

    public synchronized void downloadSessionEnded(final SessionStatistics session) {
        session.endSession();
        allSessions.addSessionStatistics(session);
    }

    public synchronized void reset() {
        allSessions = new SessionStatistics();
    }

}
