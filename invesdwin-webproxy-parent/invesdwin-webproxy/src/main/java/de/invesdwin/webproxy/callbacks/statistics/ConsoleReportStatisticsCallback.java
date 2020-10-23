package de.invesdwin.webproxy.callbacks.statistics;

import java.text.NumberFormat;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.concurrent.NotThreadSafe;

import de.invesdwin.context.log.Log;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.time.Instant;
import de.invesdwin.util.time.duration.Duration;
import de.invesdwin.webproxy.callbacks.statistics.basis.AStatisticsCallback;
import de.invesdwin.webproxy.callbacks.statistics.basis.DownloadStatistics;
import de.invesdwin.webproxy.callbacks.statistics.basis.FailureStatistics;
import de.invesdwin.webproxy.callbacks.statistics.basis.ProxyStatistics;
import de.invesdwin.webproxy.callbacks.statistics.basis.SessionStatistics;

/**
 * This class is not thread safe, but it may be used for multiple downloads sequencially.
 * 
 * @author subes
 * 
 */
@NotThreadSafe
public class ConsoleReportStatisticsCallback extends AStatisticsCallback {

    protected final Log log = new Log(this);

    protected String sessionInfo;
    protected boolean logSessionProgress;
    protected int logSessionProgressInterval = 1;
    protected boolean logSessionSummary;

    public ConsoleReportStatisticsCallback withSessionInfo(final String sessionInfo) {
        this.sessionInfo = sessionInfo;
        return this;
    }

    public ConsoleReportStatisticsCallback withLogSessionProgress(final boolean logSessionProgress) {
        this.logSessionProgress = logSessionProgress;
        return this;
    }

    public ConsoleReportStatisticsCallback withLogSessionProgressInterval(final int logSessionProgressInterval) {
        Assertions.assertThat(logSessionProgressInterval).isGreaterThan(0);
        this.logSessionProgressInterval = logSessionProgressInterval;
        return this;
    }

    public ConsoleReportStatisticsCallback withLogSessionSummary(final boolean logSessionSummary) {
        this.logSessionSummary = logSessionSummary;
        return this;
    }

    @Override
    public void downloadSuccessful(final SessionStatistics session, final DownloadStatistics statistics) {
        super.downloadSuccessful(session, statistics);
        if (logSessionProgress) {
            logProgress(session, statistics.getCountDownloadsInSession());
        }
    }

    @Override
    public void downloadFailure(final SessionStatistics session, final DownloadStatistics statistics,
            final Throwable reason) {
        super.downloadFailure(session, statistics, reason);
        if (logSessionProgress) {
            logProgress(session, statistics.getCountDownloadsInSession());
        }
    }

    @Override
    public void downloadSessionEnded(final SessionStatistics session) {
        super.downloadSessionEnded(session);
        if (logSessionSummary) {
            logReport(session);
        }
    }

    @Override
    public void proxyNotWorkingAnymore(final SessionStatistics session, final ProxyStatistics statistics) {
        super.proxyNotWorkingAnymore(session, statistics);
    }

    @Override
    public void proxyStillWorks(final SessionStatistics session, final ProxyStatistics statistics) {
        super.proxyStillWorks(session, statistics);
    }

    private void logProgress(final SessionStatistics session, final long countDownloadsInSession) {
        if (session.getDownloadsCount() % logSessionProgressInterval == 0
                || session.getDownloadsCount() == countDownloadsInSession) {
            final NumberFormat nf = NumberFormat.getNumberInstance();
            String sessionInfoText = "";
            if (sessionInfo != null) {
                sessionInfoText += "[" + sessionInfo + "] ";
            }
            log.info("%sDownloads done by %s% (%s/%s)", sessionInfoText,
                    nf.format(session.percent(session.getDownloadsCount(), countDownloadsInSession)),
                    session.getDownloadsCount(), countDownloadsInSession);
        }
    }

    /**
     * May be called after all DownloadSessions are finished.
     */
    public synchronized void logFinalReport() {
        logReport(allSessions);
    }

    private void logReport(final SessionStatistics session) {
        if (session.getDownloadsCount() == 0) {
            //no downloads, no report
            return;
        }

        final NumberFormat nf = NumberFormat.getNumberInstance();
        final StringBuilder s = new StringBuilder();
        if (sessionInfo != null) {
            s.append("[");
            s.append(sessionInfo);
            s.append("] ");
        }
        s.append("SessionReport over ");
        Instant sessionEnd = session.getSessionEnd();
        if (sessionEnd == null) {
            sessionEnd = new Instant();
        }
        s.append(new Duration(session.getSessionStart(), sessionEnd));
        s.append(" with average values:");

        if (session.getDownloadsCount() > 0) {
            s.append("\nDownloadsCountSessions: ");
            s.append(session.getDownloadsCountSessions());
            s.append("\nDownloadsSessionDurationAvg: ");
            s.append(session.getDownloadsSessionDurationAvg());
            s.append("\nDownloadsCountSuccessful: ");
            s.append(session.getDownloadsCountSuccessful());
            s.append("/");
            s.append(session.getDownloadsCount());
            s.append(" (");
            s.append(nf.format(session.percent(session.getDownloadsCountSuccessfulQuota())));
            s.append("%)");
            s.append("\nDownloadsCountTriesAvg: ");
            s.append(nf.format(session.getDownloadsCountTriesAvg()));
            s.append("\nDownloadsTriesDurationAvg: ");
            s.append(session.getDownloadsTriesDurationAvg());
            appendFailureStatistics(s, "DownloadsFailureReasons", session.getDownloadsFailureReasons());
        }

        if (session.getProxiesCountRecords() > 0) {
            s.append("\nProxiesPoolSizeAvg: ");
            s.append(nf.format(session.getProxiesPoolSizeAvg()));
            s.append("\nProxiesDownloadTriesCountSuccessfulAvg: ");
            s.append(nf.format(session.getProxiesDownloadTriesCountSuccessfulAvg()));
            s.append("/");
            s.append(nf.format(session.getProxiesDownloadTriesCountAvg()));
            s.append(" (");
            s.append(nf.format(session.percent(session.getProxiesDownloadTriesCountSuccessfulQuota())));
            s.append("%)");
            s.append("\nProxiesUseDurationAvg: ");
            s.append(session.getProxiesUseDurationAvg());
            s.append("\nProxiesCountFailures: ");
            s.append(session.getProxiesCountFailures());
            appendFailureStatistics(s, "ProxiesFailureReasons", session.getProxiesFailureReasons());
        }
        log.info(s.toString());
    }

    private void appendFailureStatistics(final StringBuilder s, final String titel,
            final FailureStatistics failureStatistics) {
        final Map<String, Long> fehler_anzahl = failureStatistics.getFailuresSortedByCount();
        if (fehler_anzahl.size() > 0) {
            s.append("\n");
            s.append(titel);
            s.append(": ");
            for (final Entry<String, Long> fehler : fehler_anzahl.entrySet()) {
                s.append("\n\t");
                s.append(fehler.getValue());
                s.append("x ");
                s.append(fehler.getKey());
            }
        }
    }
}
