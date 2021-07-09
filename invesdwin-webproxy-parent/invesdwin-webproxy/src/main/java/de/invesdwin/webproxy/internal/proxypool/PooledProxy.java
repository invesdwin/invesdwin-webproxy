package de.invesdwin.webproxy.internal.proxypool;

import javax.annotation.concurrent.NotThreadSafe;

import org.apache.commons.math3.random.RandomDataGenerator;

import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.time.Instant;
import de.invesdwin.util.time.date.FTimeUnit;
import de.invesdwin.util.time.duration.Duration;
import de.invesdwin.webproxy.WebproxyProperties;
import de.invesdwin.webproxy.broker.contract.schema.Proxy;
import de.invesdwin.webproxy.callbacks.statistics.basis.ProxyStatistics;

@NotThreadSafe
public final class PooledProxy extends Proxy {

    private static final long serialVersionUID = 1L;
    private final Instant verwendungsStart = new Instant();
    private int downloadTriesSuccessful;
    private int downloadTries;
    private Instant warmedUp;

    /**
     * An Instant in the future if cooldown is running.
     */
    private Instant cooledDown = Instant.DUMMY;

    protected PooledProxy(final Proxy proxy) {
        Assertions.assertThat(proxy).as("Have you forgotten to deactivate the webproxy stubs?").isNotNull();
        mergeFrom(proxy);
    }

    public void downloadTrySuccessful() {
        this.downloadTriesSuccessful++;
        setWarmedUp();
    }

    public void downloadTry() {
        this.downloadTries++;
    }

    public void setWarmedUp() {
        this.warmedUp = new Instant();
    }

    public boolean isWarmedUp() {
        return this.warmedUp != null;
    }

    /**
     * Gets determined at random between Min and Max.
     */
    public void startCoolingDown() {
        final FTimeUnit ns = FTimeUnit.NANOSECONDS;
        final Duration waitTime = new Duration(
                new RandomDataGenerator().nextLong(WebproxyProperties.PROXY_POOL_COOLDOWN_MIN_TIMEOUT.longValue(ns),
                        WebproxyProperties.PROXY_POOL_COOLDOWN_MAX_TIMEOUT.longValue(ns)),
                ns);
        cooledDown = new Instant(new Instant().longValue(ns) + waitTime.longValue(ns), ns);
    }

    public boolean isCooledDown() {
        return new Instant().isAfter(cooledDown);
    }

    public boolean isWarmupTimeoutExpired() {
        return !isWarmedUp() || warmedUp.isGreaterThan(WebproxyProperties.PROXY_POOL_WARMUP_TIMEOUT);
    }

    public synchronized ProxyStatistics toStatistics(final int proxiesImPool, final Throwable reason) {
        final Instant verwendungsEnde = new Instant();
        return new ProxyStatistics(this, downloadTriesSuccessful, downloadTries, verwendungsStart, verwendungsEnde,
                reason, proxiesImPool);
    }

    /**
     * The the cooldown PriorityQueue. The one with the lowest value is at the front.
     */
    @Override
    public int compareTo(final Object o) {
        if (o == null || !(o instanceof PooledProxy)) {
            return 1;
        } else {
            final PooledProxy o1 = this;
            final PooledProxy o2 = (PooledProxy) o;
            return o1.cooledDown.compareTo(o2.cooledDown);
        }
    }
}
