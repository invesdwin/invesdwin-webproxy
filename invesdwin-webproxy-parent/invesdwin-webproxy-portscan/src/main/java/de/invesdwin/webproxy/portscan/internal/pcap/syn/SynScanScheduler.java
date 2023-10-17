package de.invesdwin.webproxy.portscan.internal.pcap.syn;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.annotation.concurrent.ThreadSafe;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.time.Instant;
import de.invesdwin.webproxy.portscan.internal.PortscanProperties;

@ThreadSafe
@Named
public class SynScanScheduler implements Runnable {
    private final Set<SynScanTracker> scans;

    @Inject
    private SynScanQueue producerQueue;
    @Inject
    private SynSender synSender;
    @Inject
    private SynAckCaptor synCaptor;

    public SynScanScheduler() {
        this.scans = new CopyOnWriteArraySet<SynScanTracker>();
    }

    public int getActiveScans() {
        return scans.size();
    }

    @Override
    public void run() {
        try {
            Instant lastIteration = new Instant();
            while (true) {
                //collect tasks
                collectTasksOrWait();
                //process this iteration of scans
                final Set<SynScanTracker> copy = new HashSet<SynScanTracker>(scans);
                int gesendetePakete = 0;
                for (final SynScanTracker scan : copy) {
                    if (scan.isReadyForNewRequest()) {
                        final Integer port = scan.nextPort();
                        if (port != null) {
                            synSender.sendSyn(scan, port);
                            gesendetePakete++;
                            if (gesendetePakete >= PortscanProperties.MAX_PACKETS_PER_SECOND) {
                                /*
                                 * The hosts that can be scanned in parallel are limited. The start of the list is thus
                                 * processed with a buffer.
                                 */
                                break;
                            }
                        } else {
                            removeTask(scan);
                        }
                    }
                }
                //Wait if there are not enough tasks to make me busy
                lastIteration.sleepRelative(PortscanProperties.UPLOAD_PAUSE_BETWEEN_PACKETS_PER_HOST);
                lastIteration = new Instant();
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
    }

    private void collectTasksOrWait() throws InterruptedException {
        //Wait if there are now tasks available
        if (scans.isEmpty() && producerQueue.isEmpty()) {
            addTask(producerQueue.take());
        }
        //Else empty the cache for a bit
        while (scans.size() < PortscanProperties.MAX_PACKETS_PER_SECOND) {
            final SynScanTracker neuerTracker = producerQueue.poll();
            if (neuerTracker != null) {
                addTask(neuerTracker);
            } else {
                break;
            }
        }
    }

    private void addTask(final SynScanTracker newTracker) {
        //Same scans get received at the same time anyway
        scans.add(newTracker);
    }

    private void removeTask(final SynScanTracker tracker) {
        synCaptor.stopTracking(tracker);
        Assertions.assertThat(scans.remove(tracker)).isTrue();
    }

}
