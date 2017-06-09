package de.invesdwin.webproxy.portscan.internal.pcap.syn;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.math3.random.RandomData;
import org.apache.commons.math3.random.RandomDataImpl;

import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.webproxy.portscan.internal.PortscanProperties;
import de.invesdwin.webproxy.portscan.internal.pcap.AScanTracker.ScanStatus;
import de.invesdwin.webproxy.portscan.internal.pcap.PacketSender;
import jpcap.packet.TCPPacket;

/**
 * @see <a
 *      href="http://www.linuxforums.org/forum/linux-networking/57703-help-needed-constructing-tcp-syn-packet.html">Infos</a>
 * 
 * @author subes
 * 
 */
@ThreadSafe
@Named
public class SynSender {

    private static final int ACK_NUM = 0;
    private static final boolean URG = false;
    private static final boolean ACK = false;
    private static final boolean PSH = false;
    private static final boolean RST = false;
    private static final boolean SYN = true;
    private static final boolean FIN = false;
    private static final boolean RSV1 = false;
    private static final boolean RSV2 = false;
    private static final int URGENT = 0;
    private static final int SRC_PORT = PortscanProperties.LOCAL_BIND_PORT;
    private static final byte[] OPTION = { 0x02, 0x04, 0x05, (byte) 0xb4 }; //MSS=1460

    @Inject
    private PacketSender sender;

    /**
     * This method sends a SYN packet to the given target.
     * 
     * @throws InterruptedException
     */
    public void sendeSyn(@Nonnull final SynScanTracker tracker, final int dst_port) throws InterruptedException {
        Assertions.assertThat(tracker).isNotNull();
        final RandomData randomData = new RandomDataImpl();
        final long sequence = randomData.nextLong(0, Long.MAX_VALUE); //The standard specifies this
        final int window = randomData.nextInt(1, 4) * 1024; //Nmap randomly uses 1024, 2048, 3072 or 4096, to hide the identity

        final TCPPacket tcp = new TCPPacket(SRC_PORT, dst_port, sequence, ACK_NUM, URG, ACK, PSH, RST, SYN, FIN, RSV1,
                RSV2, window, URGENT);
        tcp.option = OPTION;
        sender.send(tcp, tracker.getHost());
        tracker.setStatus(ScanStatus.WAIT_FOR_RESPONSE);
    }

}
