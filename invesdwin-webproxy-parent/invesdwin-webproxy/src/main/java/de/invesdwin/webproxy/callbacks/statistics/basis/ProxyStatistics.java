package de.invesdwin.webproxy.callbacks.statistics.basis;

import javax.annotation.concurrent.Immutable;

import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.bean.AValueObject;
import de.invesdwin.util.time.Instant;
import de.invesdwin.util.time.duration.Duration;
import de.invesdwin.util.time.fdate.FTimeUnit;
import de.invesdwin.webproxy.broker.contract.schema.Proxy;

@Immutable
public class ProxyStatistics extends AValueObject {

    private static final long serialVersionUID = 1L;

    private final int downloadTriesCountSuccessful;
    private final int downloadTriesCount;
    private final Instant useStart;
    private final Instant useEnd;
    private final int proxiesInPool;
    private final Proxy proxy;
    private final Throwable failureReason;

    public ProxyStatistics(final Proxy proxy, final int downloadTriesCountSuccessful, final int downloadTriesCount,
            final Instant useStart, final Instant useEnd, final Throwable failureReason, final int proxiesInPool) {
        super();
        Assertions.assertThat(new Duration(useStart, useEnd).isGreaterThan(1, FTimeUnit.NANOSECONDS)).isTrue();
        this.proxy = proxy;
        this.downloadTriesCountSuccessful = downloadTriesCountSuccessful;
        this.downloadTriesCount = downloadTriesCount;
        this.useStart = useStart;
        this.useEnd = useEnd;
        this.failureReason = failureReason;
        this.proxiesInPool = proxiesInPool;
    }

    public Proxy getProxy() {
        return proxy;
    }

    public int getDownloadTriesCountSuccessful() {
        return downloadTriesCountSuccessful;
    }

    public int getDownloadTriesCount() {
        return downloadTriesCount;
    }

    public Instant getUseStart() {
        return useStart;
    }

    public Instant getUseEnd() {
        return useEnd;
    }

    public Throwable getFailureReason() {
        return failureReason;
    }

    public int getPoolSize() {
        return proxiesInPool;
    }

}
