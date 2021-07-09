package de.invesdwin.webproxy.callbacks.statistics.basis;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.time.Instant;
import de.invesdwin.util.time.date.FTimeUnit;
import de.invesdwin.util.time.duration.Duration;

@ThreadSafe
public final class SessionStatistics {

    @GuardedBy("this")
    private Instant sessionStart;
    @GuardedBy("this")
    private Instant sessionEnd;

    @GuardedBy("this")
    private long downloadsCount;
    @GuardedBy("this")
    private long downloadsCountFailures;
    @GuardedBy("this")
    private long downloadsCountSessions;
    @GuardedBy("this")
    private long downloadsSumSessionDurationInNanos;
    @GuardedBy("this")
    private long downloadsSumTryDurationInNanos;
    @GuardedBy("this")
    private long downloadsCountTries;
    @GuardedBy("this")
    private final FailureStatistics downloadsFailureReasons = new FailureStatistics();

    @GuardedBy("this")
    private long proxiesCountRecords;
    @GuardedBy("this")
    private long proxiesCountFailures;
    @GuardedBy("this")
    private long proxiesDownloadTryCountSuccessful;
    @GuardedBy("this")
    private long proxiesDownloadTryCount;
    @GuardedBy("this")
    private long proxiesSumPoolSize;
    @GuardedBy("this")
    private long proxiesSumUseDurationInNanos;
    @GuardedBy("this")
    private final FailureStatistics proxiesFailureReasons = new FailureStatistics();

    protected SessionStatistics() {}

    public static SessionStatistics newSessionStatistics(final AStatisticsCallback callback) {
        if (callback == null) {
            return null;
        } else {
            return new SessionStatistics();
        }
    }

    private void assertSessionStillOpen() {
        Assertions.assertThat(sessionEnd).as("Session has already ended!").isNull();
    }

    protected synchronized void addDownloadStatistics(final DownloadStatistics download,
            final Throwable failureReason) {
        assertSessionStillOpen();

        if (sessionStart == null) {
            sessionStart = download.getDownloadTryStart();
        }

        downloadsCount++;
        downloadsSumTryDurationInNanos += new Duration(download.getDownloadTryStart(), download.getDownloadTryEnd())
                .longValue(FTimeUnit.NANOSECONDS);
        downloadsCountTries += download.getNeededRetries() + 1;
        if (failureReason != null) {
            downloadsCountFailures++;
            downloadsFailureReasons.addFailure(failureReason);
        }
    }

    protected synchronized void addProxyStatistics(final ProxyStatistics proxy) {
        assertSessionStillOpen();
        proxiesCountRecords++;
        Assertions.assertThat(proxy.getDownloadTriesCountSuccessful())
                .isLessThanOrEqualTo(proxy.getDownloadTriesCount());
        Assertions.assertThat(proxy.getDownloadTriesCount()).isGreaterThanOrEqualTo(1);
        proxiesDownloadTryCountSuccessful += proxy.getDownloadTriesCountSuccessful();
        proxiesDownloadTryCount += proxy.getDownloadTriesCount();
        proxiesSumPoolSize += proxy.getPoolSize();
        proxiesSumUseDurationInNanos += new Duration(proxy.getUseStart(), proxy.getUseEnd())
                .longValue(FTimeUnit.NANOSECONDS);
        if (proxy.getFailureReason() != null) {
            proxiesCountFailures++;
            proxiesFailureReasons.addFailure(proxy.getFailureReason());
        }
    }

    protected synchronized void endSession() {
        assertSessionStillOpen();
        sessionEnd = new Instant();
        downloadsSumSessionDurationInNanos += new Duration(getSessionStart(), getSessionEnd())
                .longValue(FTimeUnit.NANOSECONDS);
        downloadsCountSessions++;
    }

    /**
     * Only as convenience for allSessions.
     */
    protected synchronized void addSessionStatistics(final SessionStatistics session) {
        if (sessionStart == null) {
            sessionStart = session.getSessionStart();
        }
        sessionEnd = session.getSessionEnd();

        downloadsCount += session.getDownloadsCount();
        downloadsCountFailures += session.getDownloadsCountFailures();
        downloadsCountSessions += session.getDownloadsCountSessions();
        downloadsSumSessionDurationInNanos += session.getDownloadsSumSessionDurationInNanos();
        downloadsSumTryDurationInNanos += session.getDownloadsSumTryDurationInNanos();
        downloadsCountTries += session.getDownloadsCountTries();
        downloadsFailureReasons.addFailureStatistics(session.getDownloadsFailureReasons());
        proxiesCountRecords += session.getProxiesCountRecords();
        proxiesDownloadTryCountSuccessful += session.getProxiesDownloadTryCountSuccessful();
        proxiesDownloadTryCount += session.getProxiesDownloadTryCount();
        proxiesSumPoolSize += session.getProxiesSumPoolSize();
        proxiesSumUseDurationInNanos += session.getProxiesSumUseDurationInNanos();
        proxiesCountFailures += session.getProxiesCountFailures();
        proxiesFailureReasons.addFailureStatistics(session.getProxiesFailureReasons());
    }

    public double percent(final double value, final double count) {
        return percent(avg(value, count));
    }

    public double percent(final double avg) {
        if (avg <= 0) {
            return -1;
        } else {
            return avg * 100d;
        }
    }

    public double avg(final double value, final double count) {
        if (count <= 0) {
            return -1;
        } else {
            return value / count;
        }
    }

    /********************** getter ********************/

    public synchronized Instant getSessionStart() {
        if (sessionStart != null) {
            return sessionStart;
        } else {
            return new Instant();
        }
    }

    public synchronized Instant getSessionEnd() {
        if (sessionEnd != null) {
            return sessionEnd;
        } else {
            return new Instant();
        }
    }

    public synchronized long getDownloadsCount() {
        return downloadsCount;
    }

    public long getDownloadsCountSuccessful() {
        return getDownloadsCount() - getDownloadsCountFailures();
    }

    public synchronized long getDownloadsCountFailures() {
        return downloadsCountFailures;
    }

    public synchronized long getDownloadsCountTries() {
        return downloadsCountTries;
    }

    public synchronized long getDownloadsCountSessions() {
        return downloadsCountSessions;
    }

    public synchronized long getDownloadsSumSessionDurationInNanos() {
        return downloadsSumSessionDurationInNanos;
    }

    public synchronized long getDownloadsSumTryDurationInNanos() {
        return downloadsSumTryDurationInNanos;
    }

    public synchronized FailureStatistics getDownloadsFailureReasons() {
        return downloadsFailureReasons;
    }

    public synchronized long getProxiesCountRecords() {
        return proxiesCountRecords;
    }

    public synchronized long getProxiesCountFailures() {
        return proxiesCountFailures;
    }

    public synchronized long getProxiesDownloadTryCountSuccessful() {
        return proxiesDownloadTryCountSuccessful;
    }

    public synchronized long getProxiesDownloadTryCount() {
        return proxiesDownloadTryCount;
    }

    public synchronized long getProxiesSumPoolSize() {
        return proxiesSumPoolSize;
    }

    public synchronized long getProxiesSumUseDurationInNanos() {
        return proxiesSumUseDurationInNanos;
    }

    public synchronized FailureStatistics getProxiesFailureReasons() {
        return proxiesFailureReasons;
    }

    public synchronized double getProxiesDownloadTriesCountSuccessfulAvg() {
        return avg(getProxiesDownloadTryCountSuccessful(), getProxiesCountRecords());
    }

    public synchronized double getProxiesDownloadTriesCountAvg() {
        return avg(getProxiesDownloadTryCount(), getProxiesCountRecords());
    }

    public synchronized Duration getDownloadsSessionDurationAvg() {
        return new Duration((long) avg((long) avg(getDownloadsSumSessionDurationInNanos(), getDownloadsCountSessions()),
                getDownloadsCount()), FTimeUnit.NANOSECONDS);
    }

    public synchronized double getDownloadsCountTriesAvg() {
        return avg(getDownloadsCountTries(), getDownloadsCount());
    }

    public synchronized Duration getDownloadsTriesDurationAvg() {
        return new Duration((long) avg((long) avg(getDownloadsSumTryDurationInNanos(), getDownloadsCountTries()),
                getDownloadsCount()), FTimeUnit.NANOSECONDS);
    }

    public synchronized double getProxiesPoolSizeAvg() {
        return avg(getProxiesSumPoolSize(), getProxiesCountRecords());
    }

    public synchronized Duration getProxiesUseDurationAvg() {
        return new Duration((long) avg(getProxiesSumUseDurationInNanos(), getProxiesCountRecords()),
                FTimeUnit.NANOSECONDS);
    }

    public synchronized double getProxiesDownloadTriesCountSuccessfulQuota() {
        return avg(getProxiesDownloadTriesCountSuccessfulAvg(), getProxiesDownloadTriesCountAvg());
    }

    public synchronized double getDownloadsCountSuccessfulQuota() {
        return avg(getDownloadsCountSuccessful(), getDownloadsCount());
    }

}
