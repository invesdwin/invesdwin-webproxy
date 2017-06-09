package de.invesdwin.webproxy.internal.get;

import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import de.invesdwin.util.bean.AValueObject;
import de.invesdwin.util.time.Instant;
import de.invesdwin.webproxy.callbacks.statistics.basis.DownloadStatistics;

@ThreadSafe
class TempDownloadInformation extends AValueObject {
    private static final long serialVersionUID = 1L;

    private final AtomicInteger endedDownloadsInSession;
    private final int countDownloadsInSession;
    private final Instant downloadTryStart = new Instant();
    @GuardedBy("this")
    private int neededRetries;

    TempDownloadInformation(final AtomicInteger endedDownloadsInSession, final int countDownloadsInSession) {
        super();
        this.endedDownloadsInSession = endedDownloadsInSession;
        this.countDownloadsInSession = countDownloadsInSession;
    }

    public synchronized void incrementNeededRetries() {
        this.neededRetries++;
    }

    public synchronized int getNeededRetries() {
        return neededRetries;
    }

    /**
     * Boolean als indikator, ob damit alle downloads beendet sind.
     */
    public synchronized boolean downloadEnded() {
        return endedDownloadsInSession.incrementAndGet() >= countDownloadsInSession;
    }

    public synchronized DownloadStatistics toStatistics(final boolean proxy, final URI uri) {
        final Instant downloadVerucheEnde = new Instant();
        return new DownloadStatistics(uri.toString(), proxy, countDownloadsInSession, downloadTryStart,
                downloadVerucheEnde, neededRetries);
    }
}