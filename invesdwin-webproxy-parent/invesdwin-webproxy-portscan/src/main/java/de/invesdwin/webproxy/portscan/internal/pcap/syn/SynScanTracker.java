package de.invesdwin.webproxy.portscan.internal.pcap.syn;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import de.invesdwin.util.collections.Collections;
import de.invesdwin.util.time.Instant;
import de.invesdwin.util.time.duration.Duration;
import de.invesdwin.webproxy.portscan.internal.PortscanProperties;
import de.invesdwin.webproxy.portscan.internal.pcap.AScanTracker;

@ThreadSafe
public class SynScanTracker extends AScanTracker {

    public static final Duration RESPONSE_TIMEOUT = PortscanProperties.RESPONSE_TIMEOUT_BETWEEN_SYN_PACKETS_PER_HOST;
    private static final long serialVersionUID = 1L;
    /*
     * The first request does not count here!
     */
    private static final int MAX_RETRIES = 2;
    private static final int NO_CURRENT_RETRY_PORT_INDEX = -1;
    private static final Object LOCK = new Object();
    private static final Duration REMOVAL_TIMEOUT = RESPONSE_TIMEOUT.multiply(2);
    private static final AtomicInteger OPEN_REQUESTS_COUNTER = new AtomicInteger();

    private final Map<ISynListener, Set<Integer>> listener_interestedPorts = new ConcurrentHashMap<ISynListener, Set<Integer>>();
    private final boolean randomScan;
    @GuardedBy("this")
    private final List<Integer> toBeScannedPorts;
    private final Set<Integer> positiveRespondedPorts = new ConcurrentSkipListSet<Integer>();
    private final Set<Integer> negativeRespondedPorts = new ConcurrentSkipListSet<Integer>();
    private volatile Instant markedForRemoval;
    @GuardedBy("this")
    private int currentPortIndex;
    @GuardedBy("this")
    private int currentRetry;
    @GuardedBy("this")
    private int currentRetryPortIndex = NO_CURRENT_RETRY_PORT_INDEX;

    SynScanTracker(final ISynListener listener, final InetAddress host, final List<Integer> toBeScannedPorts,
            final boolean randomScan) {
        super(host, LOCK, OPEN_REQUESTS_COUNTER, PortscanProperties.MAX_OPEN_SYN_REQUESTS, RESPONSE_TIMEOUT);
        this.listener_interestedPorts.put(listener,
                Collections.unmodifiableSet(new HashSet<Integer>(toBeScannedPorts)));
        this.toBeScannedPorts = new ArrayList<Integer>(toBeScannedPorts);
        this.randomScan = randomScan;
    }

    public Map<ISynListener, Set<Integer>> getListenersWithInterestedPorts() {
        return listener_interestedPorts;
    }

    synchronized boolean addListener(final ISynListener listener, final List<Integer> toBeScannedPorts) {
        final boolean changed = listener_interestedPorts.put(listener,
                Collections.unmodifiableSet(new HashSet<Integer>(toBeScannedPorts))) == null;
        for (final Integer port : toBeScannedPorts) {
            if (this.toBeScannedPorts.contains(port)) {
                //notify about already scanned ports that interest him
                if (positiveRespondedPorts.contains(port)) {
                    listener.synAckReceived(getHost(), port);
                }
            } else {
                //or add the new ports to the list of tasks
                this.toBeScannedPorts.add(port);
            }
        }
        return changed;
    }

    @Override
    public boolean isResponseTimeoutExpired() {
        final boolean timeout = super.isResponseTimeoutExpired();
        if (timeout) {
            maybeRetry();
        }
        return timeout;
    }

    private synchronized void maybeRetry() {
        final boolean hostIstNichtFirewalledMitPaketDrops = negativeRespondedPorts.size() > 0;
        if (hostIstNichtFirewalledMitPaketDrops) {
            final int letzterPortIndex = currentPortIndex - 1;
            final boolean retriesAufgebraucht = currentRetry >= MAX_RETRIES;
            final boolean mittlerweileNeuerPort = letzterPortIndex > currentRetryPortIndex;
            if (retriesAufgebraucht) {
                //No more retries, next port
                currentRetry = 0;
                currentRetryPortIndex = NO_CURRENT_RETRY_PORT_INDEX;
            } else if (mittlerweileNeuerPort) {
                currentRetry = 1;
                //we remember the port for the next few retries
                currentRetryPortIndex = letzterPortIndex;
            } else {
                currentRetry++;
                //we undo the increment of the port index
                currentPortIndex = letzterPortIndex;
            }
        }
    }

    synchronized Integer nextPort() {
        if (currentPortIndex >= toBeScannedPorts.size()) {
            return null;
        } else {
            final Integer port = toBeScannedPorts.get(currentPortIndex);
            currentPortIndex++;
            return port;
        }
    }

    void setMarkedForRemoval(final Instant markiertFuerEntfernen) {
        this.markedForRemoval = markiertFuerEntfernen;
    }

    boolean isReadyForRemoval() {
        return markedForRemoval != null && markedForRemoval.isGreaterThan(REMOVAL_TIMEOUT);
    }

    boolean hostRespondedPositiveOnPort(final Integer port) {
        return positiveRespondedPorts.add(port);
    }

    void hostRespondedNegativeOnPort(final Integer port) {
        negativeRespondedPorts.add(port);
    }

    synchronized void stopOnRandomScan() {
        if (randomScan) {
            currentPortIndex = toBeScannedPorts.size();
            setStatus(ScanStatus.WAIT_FOR_REQUEST);
        }
    }

}