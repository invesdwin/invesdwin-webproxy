package de.invesdwin.webproxy.portscan.internal.pcap.syn;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import org.springframework.beans.factory.InitializingBean;

import de.invesdwin.util.concurrent.Executors;
import de.invesdwin.util.time.Instant;
import de.invesdwin.util.time.duration.Duration;
import de.invesdwin.webproxy.portscan.internal.PortscanProperties;
import de.invesdwin.webproxy.portscan.internal.pcap.APacketCaptor;
import de.invesdwin.webproxy.portscan.internal.pcap.AScanTracker.ScanStatus;
import de.invesdwin.webproxy.portscan.internal.scanner.PortScanner;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jpcap.PacketReceiver;
import jpcap.packet.Packet;
import jpcap.packet.TCPPacket;

@Named
@ThreadSafe
public class SynAckCaptor extends APacketCaptor implements InitializingBean {

    private final ScheduledExecutorService executor = Executors
            .newScheduledThreadPool(this.getClass().getSimpleName() + "_CLEANUP", 1);
    private final ConcurrentMap<InetAddress, SynScanTracker> host_tracker = new ConcurrentHashMap<InetAddress, SynScanTracker>();

    @Inject
    private PortScanner portScanner;

    private final PacketReceiver pr = new PacketReceiver() {
        @Override
        public void receivePacket(@Nonnull final Packet p) {
            if (p instanceof TCPPacket) {
                final TCPPacket tcp = (TCPPacket) p;
                final InetAddress host = tcp.src_ip;
                final int port = tcp.src_port;
                final SynScanTracker tracker = host_tracker.get(host);
                notifyAboutResponse(tcp, host, port, tracker);
            } else {
                throw new IllegalArgumentException(Packet.class.getSimpleName() + " is not a "
                        + TCPPacket.class.getSimpleName() + " but a " + p.getClass().getSimpleName());
            }
        }

        private void notifyAboutResponse(final TCPPacket tcp, final InetAddress host, final int port,
                final SynScanTracker tracker) {
            if (tracker != null) {
                tracker.setStatus(ScanStatus.WAIT_FOR_REQUEST);
                if (tcp.rst) {
                    tracker.hostRespondedNegativeOnPort(port);
                } else {
                    final boolean notYetNotifiedAboutPort = tracker.hostRespondedPositiveOnPort(port);
                    if (notYetNotifiedAboutPort) {
                        notifyAboutResponse(host, port, tracker);
                    }
                }
            }
        }

        private void notifyAboutResponse(final InetAddress host, final int port, final SynScanTracker tracker) {
            for (final Entry<ISynListener, Set<Integer>> e : tracker.getListenersWithInterestedPorts().entrySet()) {
                final ISynListener listener = e.getKey();
                final Set<Integer> interestedPorts = e.getValue();
                if (interestedPorts.contains(port)) {
                    listener.synAckReceived(host, port);
                }
            }
        }
    };

    @Override
    public void afterPropertiesSet() throws Exception {
        final Duration schedule = SynScanTracker.RESPONSE_TIMEOUT;
        executor.scheduleWithFixedDelay(new ScanMonitor(), schedule.longValue(), schedule.longValue(),
                schedule.getTimeUnit().timeUnitValue());
    }

    /**
     * While a scan is running, duplicate responses are being filtered from the specific host.
     */
    public SynScanTracker startTracking(final ISynListener listener, final InetAddress host,
            final List<Integer> toBeScannedPorts, final boolean randomScan) {
        final SynScanTracker newScan = new SynScanTracker(listener, host, toBeScannedPorts, randomScan);
        final SynScanTracker previousScan = host_tracker.putIfAbsent(host, newScan);
        if (previousScan != null) {
            if (previousScan.isReadyForRemoval()) {
                //replace previous
                host_tracker.put(host, newScan);
                return newScan;
            } else {
                //or notify about new interested party
                previousScan.addListener(listener, toBeScannedPorts);
                return previousScan;
            }
        } else {
            return newScan;
        }
    }

    /**
     * Stops filtering of responses on the host after a delay.
     */
    public void stopTracking(final SynScanTracker tracker) {
        tracker.setMarkedForRemoval(new Instant());
    }

    @Override
    protected String getPacketFilter() {
        return "dst port " + PortscanProperties.LOCAL_BIND_PORT
                + " and tcp[tcpflags] & (tcp-syn) != 0 and tcp[tcpflags] & (tcp-ack) != 0"
                + " or tcp[tcpflags] & (tcp-rst) != 0";
    }

    @Override
    protected PacketReceiver getPacketReceiver() {
        return pr;
    }

    private final class ScanMonitor implements Runnable {
        @Override
        public void run() {
            final Map<InetAddress, SynScanTracker> copy = new HashMap<InetAddress, SynScanTracker>(host_tracker);
            for (final Entry<InetAddress, SynScanTracker> e : copy.entrySet()) {
                //remove RandomScans if randomscan stopped
                if (!portScanner.isRandomScanRunning()) {
                    e.getValue().stopOnRandomScan();
                }
                //regularly check if timeouts have expired
                e.getValue().isResponseTimeoutExpired();
                //responses might even come after sending the syns has finished
                if (e.getValue().isReadyForRemoval()) {
                    host_tracker.remove(e.getKey(), e.getValue());
                }
            }
        }
    }

}
