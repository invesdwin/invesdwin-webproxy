package de.invesdwin.webproxy.portscan.internal.scanner;

import java.net.InetAddress;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Configurable;

import de.invesdwin.context.integration.network.RandomIpGenerator;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.webproxy.portscan.internal.pcap.icmp.IPingListener;
import de.invesdwin.webproxy.portscan.internal.pcap.icmp.IcmpCaptor;
import de.invesdwin.webproxy.portscan.internal.pcap.icmp.IcmpScanTracker;
import de.invesdwin.webproxy.portscan.internal.pcap.icmp.IcmpSender;
import de.invesdwin.webproxy.portscan.internal.pcap.syn.ISynListener;
import de.invesdwin.webproxy.portscan.internal.pcap.syn.SynAckCaptor;
import de.invesdwin.webproxy.portscan.internal.pcap.syn.SynScanTracker;

@ThreadSafe
@Configurable
class RandomScanHostFinder implements Runnable, IPingListener {

    @GuardedBy("this")
    private final Set<InetAddress> pingReceived;
    private final BlockingQueue<SynScanTracker> consumerQueue;
    private final List<Integer> toBeScannedPorts;
    private final ISynListener listener;

    @Inject
    private IcmpSender icmpSender;
    @Inject
    private IcmpCaptor icmpCaptor;
    @Inject
    private SynAckCaptor synCaptor;

    RandomScanHostFinder(final ISynListener listener, final BlockingQueue<SynScanTracker> consumerQueue,
            final List<Integer> toBeScannedPorts) {
        this.listener = listener;
        this.pingReceived = new LinkedHashSet<InetAddress>();
        this.consumerQueue = consumerQueue;
        this.toBeScannedPorts = toBeScannedPorts;
    }

    @Override
    public void run() {
        try {
            while (true) {
                synchronized (this) {
                    while (pingReceived.size() > 0) {
                        //Blocks when queue is full
                        final InetAddress respondingHost = pingReceived.iterator().next();
                        Assertions.assertThat(pingReceived.remove(respondingHost)).isTrue();
                        final SynScanTracker tracker = synCaptor.startTracking(listener, respondingHost,
                                toBeScannedPorts, true);
                        consumerQueue.put(tracker);
                    }
                }
                final InetAddress host = RandomIpGenerator.getRandomIp();
                final IcmpScanTracker tracker = icmpCaptor.startTracking(this, host);
                icmpSender.sendIcmp(tracker);
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public synchronized void pingReceived(final InetAddress host) {
        pingReceived.add(host);
    }

}