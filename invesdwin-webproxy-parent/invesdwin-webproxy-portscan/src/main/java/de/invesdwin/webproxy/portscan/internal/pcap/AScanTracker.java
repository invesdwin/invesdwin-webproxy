package de.invesdwin.webproxy.portscan.internal.pcap;

import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.bean.AValueObject;
import de.invesdwin.util.time.Instant;
import de.invesdwin.util.time.duration.Duration;

@ThreadSafe
public abstract class AScanTracker extends AValueObject {

    private static final long serialVersionUID = 1L;
    private final Object lock;
    private final InetAddress host;
    private final int maxOpenRequests;
    @GuardedBy("lock")
    private final AtomicInteger openRequestsCounter;
    @GuardedBy("lock")
    private Instant lastSentRequest;
    @GuardedBy("lock")
    private final Duration responseTimeout;
    @GuardedBy("lock")
    private ScanStatus status = ScanStatus.WAIT_FOR_REQUEST;

    public AScanTracker(final InetAddress host, final Object lock, final AtomicInteger openRequestsCounter,
            final int maxOpenRequests, final Duration responseTimeout) {
        this.lock = lock;
        this.host = host;
        this.openRequestsCounter = openRequestsCounter;
        this.maxOpenRequests = maxOpenRequests;
        this.responseTimeout = responseTimeout;
    }

    public InetAddress getHost() {
        return host;
    }

    public void setStatus(final ScanStatus status) {
        synchronized (lock) {
            if (status == ScanStatus.WAIT_FOR_RESPONSE) {
                Assertions.assertThat(this.status).isEqualTo(ScanStatus.WAIT_FOR_REQUEST);
                lastSentRequest = new Instant();
                openRequestsCounter.incrementAndGet();
            } else if (this.status == ScanStatus.WAIT_FOR_RESPONSE) {
                //here we are a bit lax because the end can come more than once as a status
                openRequestsCounter.decrementAndGet();
                lock.notify();
            }
            this.status = status;
        }
    }

    public boolean isReadyForNewRequest() throws InterruptedException {
        synchronized (lock) {
            while (openRequestsCounter.get() >= maxOpenRequests) {
                lock.wait();
            }
            return status == ScanStatus.WAIT_FOR_REQUEST;
        }
    }

    public boolean isResponseTimeoutExpired() {
        synchronized (lock) {
            if (status == ScanStatus.WAIT_FOR_RESPONSE
                    && new Duration(lastSentRequest).isGreaterThan(responseTimeout)) {
                setStatus(ScanStatus.WAIT_FOR_REQUEST);
                return true;
            } else {
                return false;
            }
        }
    }

    public enum ScanStatus {
        WAIT_FOR_RESPONSE,
        WAIT_FOR_REQUEST;
    }

}
