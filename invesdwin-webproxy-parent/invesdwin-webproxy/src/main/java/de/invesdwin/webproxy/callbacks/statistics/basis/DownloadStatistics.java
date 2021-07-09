package de.invesdwin.webproxy.callbacks.statistics.basis;

import javax.annotation.concurrent.Immutable;

import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.bean.AValueObject;
import de.invesdwin.util.time.Instant;
import de.invesdwin.util.time.date.FTimeUnit;
import de.invesdwin.util.time.duration.Duration;

@Immutable
public class DownloadStatistics extends AValueObject {

    private static final long serialVersionUID = 1L;

    private final boolean proxyUsed;
    private final int countDownloadsInSession;
    private final int neededRetries;
    private final Instant downloadTryStart;
    private final Instant downloadTryEnd;
    private final String uri;

    public DownloadStatistics(final String uri, final boolean proxyUsed, final int countDownloadsInSession,
            final Instant downloadTryStart, final Instant downloadTryEnd, final int neededRetries) {
        super();
        Assertions.assertThat(new Duration(downloadTryStart, downloadTryEnd).isGreaterThan(1, FTimeUnit.NANOSECONDS))
                .isTrue();
        this.uri = uri;
        this.proxyUsed = proxyUsed;
        this.countDownloadsInSession = countDownloadsInSession;
        this.downloadTryStart = downloadTryStart;
        this.downloadTryEnd = downloadTryEnd;
        this.neededRetries = neededRetries;
    }

    public String getUri() {
        return uri;
    }

    public int getCountDownloadsInSession() {
        return countDownloadsInSession;
    }

    public Instant getDownloadTryEnd() {
        return downloadTryEnd;
    }

    public Instant getDownloadTryStart() {
        return downloadTryStart;
    }

    public int getNeededRetries() {
        return neededRetries;
    }

    public boolean isProxyUsed() {
        return proxyUsed;
    }

}
