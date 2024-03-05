package de.invesdwin.webproxy.portscan.internal.pcap.icmp;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;

import javax.annotation.concurrent.ThreadSafe;

import org.springframework.beans.factory.InitializingBean;

import de.invesdwin.util.concurrent.Executors;
import de.invesdwin.util.time.duration.Duration;
import de.invesdwin.webproxy.portscan.internal.pcap.APacketCaptor;
import de.invesdwin.webproxy.portscan.internal.pcap.AScanTracker.ScanStatus;
import jakarta.inject.Named;
import jpcap.PacketReceiver;
import jpcap.packet.ICMPPacket;
import jpcap.packet.Packet;

/**
 * If more than one request comes with the same host, the first ping timeout is being kept and the other requestor is
 * just added as another interested party. If the requestor was just added as a new interested party, the packet should
 * not be sent again.
 * 
 * @author subes
 * 
 */
@Named
@ThreadSafe
public class IcmpCaptor extends APacketCaptor implements InitializingBean {

    private final ScheduledExecutorService executor = Executors
            .newScheduledThreadPool(this.getClass().getSimpleName() + "_CLEANUP", 1);
    private final ConcurrentHashMap<InetAddress, IcmpScanTracker> host_tracker = new ConcurrentHashMap<InetAddress, IcmpScanTracker>();

    private final PacketReceiver pr = new PacketReceiver() {
        @Override
        public void receivePacket(final Packet p) {
            if (p instanceof ICMPPacket) {
                final ICMPPacket icmp = (ICMPPacket) p;
                final InetAddress host = icmp.src_ip;
                final IcmpScanTracker tracker = host_tracker.get(host);
                notifyAboutResponse(icmp, host, tracker);
            } else {
                throw new IllegalArgumentException(Packet.class.getSimpleName() + " is not a "
                        + ICMPPacket.class.getSimpleName() + " but a " + p.getClass().getSimpleName());
            }
        }

        private void notifyAboutResponse(final ICMPPacket icmp, final InetAddress host, final IcmpScanTracker tracker) {
            if (tracker != null && !tracker.isResponseTimeoutExpired()) {
                tracker.setStatus(ScanStatus.WAIT_FOR_REQUEST);
                if (icmp.type == ICMPPacket.ICMP_ECHOREPLY) {
                    for (final IPingListener listener : tracker.getListeners()) {
                        listener.pingReceived(host);
                    }
                }
                host_tracker.remove(host);
            }
        }
    };

    @Override
    public void afterPropertiesSet() throws Exception {
        final Duration schedule = IcmpScanTracker.RESPONSE_TIMEOUT;
        executor.scheduleWithFixedDelay(new TimeoutChecker(), schedule.longValue(), schedule.longValue(),
                schedule.getTimeUnit().timeUnitValue());
    }

    /**
     * Returns true if the timeout was not already there and thus this is the first interested party for this host.
     */
    public IcmpScanTracker startTracking(final IPingListener listener, final InetAddress host) {
        final IcmpScanTracker neu = new IcmpScanTracker(listener, host);
        final IcmpScanTracker alt = host_tracker.putIfAbsent(host, neu);
        if (alt != null) {
            alt.addListener(listener); //the same host might have been randomly generated again, thus no return-check
            return null;
        } else {
            return neu;
        }
    }

    @Override
    protected String getPacketFilter() {
        return "icmp";
    }

    @Override
    protected PacketReceiver getPacketReceiver() {
        return pr;
    }

    @ThreadSafe
    private final class TimeoutChecker implements Runnable {

        @Override
        public void run() {
            final Map<InetAddress, IcmpScanTracker> copy = new HashMap<InetAddress, IcmpScanTracker>(host_tracker); //against concurrentmodificationerror
            for (final Entry<InetAddress, IcmpScanTracker> e : copy.entrySet()) {
                if (e.getValue().isResponseTimeoutExpired()) {
                    host_tracker.remove(e.getKey());
                }
            }
        }
    }

}
