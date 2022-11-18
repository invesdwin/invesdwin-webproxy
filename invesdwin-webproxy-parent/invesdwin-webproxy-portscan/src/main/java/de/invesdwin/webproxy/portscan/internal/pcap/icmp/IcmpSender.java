package de.invesdwin.webproxy.portscan.internal.pcap.icmp;

import javax.annotation.concurrent.ThreadSafe;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.apache.commons.math3.random.RandomDataImpl;

import de.invesdwin.webproxy.portscan.internal.pcap.AScanTracker.ScanStatus;
import de.invesdwin.webproxy.portscan.internal.pcap.PacketSender;
import jpcap.packet.ICMPPacket;

@Named
@ThreadSafe
public class IcmpSender {

    @Inject
    private PacketSender sender;

    public void sendIcmp(final IcmpScanTracker tracker) throws InterruptedException {
        if (tracker == null || !tracker.isReadyForNewRequest()) {
            return;
        }
        final ICMPPacket icmp = new ICMPPacket();
        icmp.type = ICMPPacket.ICMP_ECHO;
        icmp.seq = 0;
        icmp.id = (short) new RandomDataImpl().nextInt(0, Short.MAX_VALUE);
        sender.send(icmp, tracker.getHost());
        tracker.setStatus(ScanStatus.WAIT_FOR_RESPONSE);
    }

}
