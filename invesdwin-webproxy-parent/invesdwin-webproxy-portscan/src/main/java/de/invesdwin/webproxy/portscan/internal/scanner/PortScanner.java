package de.invesdwin.webproxy.portscan.internal.scanner;

import java.net.InetAddress;
import java.util.List;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import de.invesdwin.context.beans.hook.IStartupHook;
import de.invesdwin.context.log.Log;
import de.invesdwin.util.concurrent.Executors;
import de.invesdwin.util.concurrent.WrappedExecutorService;
import de.invesdwin.webproxy.portscan.internal.pcap.syn.ISynListener;
import de.invesdwin.webproxy.portscan.internal.pcap.syn.SynAckCaptor;
import de.invesdwin.webproxy.portscan.internal.pcap.syn.SynScanQueue;
import de.invesdwin.webproxy.portscan.internal.pcap.syn.SynScanScheduler;
import de.invesdwin.webproxy.portscan.internal.pcap.syn.SynScanTracker;

@ThreadSafe
@Named
public class PortScanner implements IStartupHook {

    private final Log log = new Log(this);
    private final WrappedExecutorService synScanExecutor = Executors.newFixedThreadPool(this.getClass().getSimpleName(),
            1);

    @GuardedBy("this")
    private volatile WrappedExecutorService randomScanExecutor;

    @Inject
    private SynScanQueue producerConsumerQueue;
    @Inject
    private SynScanScheduler scanner;

    @Inject
    private SynAckCaptor synCaptor;

    @Override
    public void startup() throws Exception {
        synScanExecutor.execute(scanner);
    }

    public void portScan(final ISynListener listener, final InetAddress host, final List<Integer> ports)
            throws InterruptedException {
        final SynScanTracker tracker = synCaptor.startTracking(listener, host, ports, false);
        producerConsumerQueue.put(tracker);
    }

    public synchronized void startRandomScan(final ISynListener listener, final List<Integer> ports) {
        stopRandomScan(); //restart is already running
        randomScanExecutor = Executors.newFixedThreadPool(this.getClass().getSimpleName() + "_RANDOM_SCAN", 1);
        final RandomScanHostFinder finder = new RandomScanHostFinder(listener, producerConsumerQueue, ports);
        randomScanExecutor.execute(finder);
        log.info("RandomScan started");
    }

    public synchronized void stopRandomScan() {
        if (randomScanExecutor != null) {
            randomScanExecutor.shutdownNow();
            try {
                randomScanExecutor.awaitTermination();
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            randomScanExecutor = null;
            log.info("RandomScan stopped");
        }
    }

    public boolean isRandomScanRunning() {
        return randomScanExecutor != null;
    }

    public int getActiveScans() {
        return scanner.getActiveScans() + producerConsumerQueue.size();
    }

}
