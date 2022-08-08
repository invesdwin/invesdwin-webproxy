package de.invesdwin.webproxy.portscan.internal.pcap;

import java.io.IOException;
import java.net.InetAddress;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import javax.inject.Named;

import de.invesdwin.context.log.error.Err;
import de.invesdwin.util.lang.uri.Addresses;
import de.invesdwin.util.math.random.IRandomGenerator;
import de.invesdwin.util.math.random.PseudoRandomGenerators;
import de.invesdwin.webproxy.portscan.internal.PortscanBootstrap;
import de.invesdwin.webproxy.portscan.internal.PortscanProperties;
import jpcap.JpcapSender;
import jpcap.packet.DatalinkPacket;
import jpcap.packet.IPPacket;
import jpcap.packet.Packet;
import jpcap.packet.TCPPacket;

@ThreadSafe
@Named
public class PacketSender {

    private static final int MIN_TTL = 80;

    private static final int PRIORITY = 0;
    private static final boolean D_FLAG = false;
    private static final boolean T_FLAG = false;
    private static final boolean R_FLAG = false;
    private static final int RSV_TOS = 0;
    private static final boolean RSV_FRAG = false;
    private static final boolean DONT_FRAG = false;
    private static final boolean MORE_FRAG = false;
    private static final int OFFSET = 0;
    private static final int PROTOCOL = TCPPacket.IPPROTO_TCP;

    @Inject
    private PortscanBootstrap bootstrap;

    @GuardedBy("this")
    private JpcapSender pcapSender;

    private volatile DatalinkPacket datalink;
    private volatile InetAddress src;

    public void send(final IPPacket packet, final InetAddress dst) throws InterruptedException {
        packet.datalink = datalink;
        final IRandomGenerator randomData = PseudoRandomGenerators.getThreadLocalPseudoRandom();
        final int ident = randomData.nextInt(0, Addresses.PORT_MAX); //muss random sein per spec
        final int ttl = randomData.nextInt(MIN_TTL, 222); //zum verschleiern der identität
        packet.setIPv4Parameter(PRIORITY, D_FLAG, T_FLAG, R_FLAG, RSV_TOS, RSV_FRAG, DONT_FRAG, MORE_FRAG, OFFSET,
                ident, ttl, PROTOCOL, src, dst);
        send(packet);
    }

    /**
     * Sends a packet and respects the max number of bytes per second.
     */
    private void send(final Packet packet) throws InterruptedException {
        bootstrap.waitIfInitializing();
        try {
            synchronized (this) {
                pcapSender.sendPacket(packet);
                PortscanProperties.UPLOAD_PAUSE_BETWEEN_PACKETS.sleep();
            }
        } catch (final IOException e) {
            //reinitialize
            try {
                bootstrap.startup();
            } catch (final Exception e1) { //SUPPRESS CHECKSTYLE illegal catch
                Err.process(e1);
            }
            //customize packet with new data
            packet.datalink = datalink;
            if (packet instanceof IPPacket) {
                final IPPacket ip = (IPPacket) packet;
                ip.src_ip = src;
            }
            //retry
            send(packet);
        }
    }

    public synchronized void close() {
        if (pcapSender != null) {
            pcapSender.close();
        }
    }

    public synchronized void initialize(final JpcapSender pcapSender, final DatalinkPacket datalink,
            final InetAddress localBindAddress) {
        this.pcapSender = pcapSender;
        this.datalink = datalink;
        this.src = localBindAddress;
    }

}
