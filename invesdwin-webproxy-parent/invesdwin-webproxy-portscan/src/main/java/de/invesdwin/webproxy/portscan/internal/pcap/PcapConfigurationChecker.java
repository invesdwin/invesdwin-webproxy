package de.invesdwin.webproxy.portscan.internal.pcap;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.concurrent.ThreadSafe;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.collections.Arrays;
import de.invesdwin.webproxy.portscan.internal.PortscanProperties;
import de.invesdwin.webproxy.portscan.internal.pcap.icmp.IPingListener;
import de.invesdwin.webproxy.portscan.internal.pcap.icmp.IcmpCaptor;
import de.invesdwin.webproxy.portscan.internal.pcap.icmp.IcmpScanTracker;
import de.invesdwin.webproxy.portscan.internal.pcap.icmp.IcmpSender;
import de.invesdwin.webproxy.portscan.internal.pcap.syn.ISynListener;
import de.invesdwin.webproxy.portscan.internal.pcap.syn.SynAckCaptor;
import de.invesdwin.webproxy.portscan.internal.pcap.syn.SynScanTracker;
import de.invesdwin.webproxy.portscan.internal.pcap.syn.SynSender;

@ThreadSafe
@Named
public class PcapConfigurationChecker implements IPingListener, ISynListener {

    @Inject
    private IcmpSender icmpSender;
    @Inject
    private IcmpCaptor icmpCaptor;
    @Inject
    private SynSender synSender;
    @Inject
    private SynAckCaptor synAckCaptor;

    private final AtomicBoolean synAckReceived = new AtomicBoolean(false);
    private final AtomicBoolean pingReceived = new AtomicBoolean(false);

    public boolean icmpWorks() throws InterruptedException {
        try {
            pingReceived.set(false);
            final IcmpScanTracker tracker = icmpCaptor.startTracking(this, PortscanProperties.CHECK_HOST);
            icmpSender.sendIcmp(tracker);
            return waitForResponse(tracker, pingReceived);
        } finally {
            pingReceived.set(false);
        }
    }

    public boolean synWorks() throws InterruptedException {
        boolean synWorks = false;
        int curTry = 0;
        final int maxTries = 20;
        while (!synWorks && curTry <= maxTries) {
            curTry++;
            synWorks = internalSynWorks();
        }
        return synWorks;
    }

    protected boolean internalSynWorks() throws InterruptedException {
        try {
            synAckReceived.set(false);
            final SynScanTracker tracker = synAckCaptor.startTracking(this, PortscanProperties.CHECK_HOST,
                    Arrays.asList(PortscanProperties.CHECK_PORT), false);
            synSender.sendeSyn(tracker, PortscanProperties.CHECK_PORT);
            return waitForResponse(tracker, synAckReceived);
        } finally {
            synAckReceived.set(false);
        }
    }

    private boolean waitForResponse(final AScanTracker tracker, final AtomicBoolean flag) throws InterruptedException {
        while (!tracker.isResponseTimeoutExpired() && !tracker.isReadyForNewRequest()) {
            if (flag.get()) {
                return true;
            }
            TimeUnit.MILLISECONDS.sleep(100);
        }
        return flag.get();
    }

    @Override
    public void synAckReceived(final InetAddress host, final int port) {
        Assertions.assertThat(host).isEqualTo(PortscanProperties.CHECK_HOST);
        Assertions.assertThat(port).isEqualTo(PortscanProperties.CHECK_PORT);
        synAckReceived.set(true);
    }

    @Override
    public void pingReceived(final InetAddress host) {
        Assertions.assertThat(host).isEqualTo(PortscanProperties.CHECK_HOST);
        pingReceived.set(true);
    }

}
