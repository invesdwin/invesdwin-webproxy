package de.invesdwin.webproxy.portscan.internal;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import de.invesdwin.context.beans.hook.IStartupHook;
import de.invesdwin.context.integration.network.NetworkUtil;
import de.invesdwin.context.log.Log;
import de.invesdwin.context.log.error.Err;
import de.invesdwin.context.system.NativeLibrary;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.collections.Arrays;
import de.invesdwin.util.lang.uri.Addresses;
import de.invesdwin.util.lang.uri.URIs;
import de.invesdwin.util.shutdown.IShutdownHook;
import de.invesdwin.webproxy.portscan.internal.pcap.APacketCaptor;
import de.invesdwin.webproxy.portscan.internal.pcap.PacketSender;
import de.invesdwin.webproxy.portscan.internal.pcap.PcapConfigurationChecker;
import jpcap.JpcapCaptor;
import jpcap.JpcapSender;
import jpcap.NetworkInterface;
import jpcap.NetworkInterfaceAddress;
import jpcap.packet.DatalinkPacket;
import jpcap.packet.EthernetPacket;
import jpcap.packet.Packet;

/**
 * Handles startup, shutdown and reinitialization of the portscanner.
 * 
 * @author subes
 * 
 */
@Named
@ThreadSafe
public class PortscanBootstrap implements IStartupHook, IShutdownHook {

    private static final int PCAP_SNAPLEN = 2000;
    private static final boolean PCAP_PROMISC = false; //we only need the packets that are sent directly to us
    private static final int PCAP_TO_MS = 100; //0 causes a deadlock on windows

    @SuppressWarnings("unused" /* used to extract and hold the lib */)
    private static final NativeLibrary JPCAP = new NativeLibrary("jpcap", "/jpcap/", PortscanService.class);
    private final Log log = new Log(this);

    @GuardedBy("PortscanBootstrap.class")
    private boolean initializing;

    @Inject
    private APacketCaptor[] captor;
    @Inject
    private PacketSender sender;
    @Inject
    private PcapConfigurationChecker checker;

    /**
     * Throws InterruptedException so that in RandomPortScanner the shutdownNow correctly works.
     */
    public boolean waitIfInitializing() throws InterruptedException {
        boolean waited = false;
        synchronized (PortscanBootstrap.class) {
            while (initializing) {
                waited = true;
                PortscanBootstrap.class.wait();
            }
        }
        return waited;
    }

    @Override
    public void startup() throws Exception {
        synchronized (PortscanBootstrap.class) {
            //double safety
            if (waitIfInitializing()) {
                return;
            } else {
                initializing = true;
            }
        }

        try {
            //Close works around the cpu eating looppacket endless loop in the captor
            shutdown();
            NetworkUtil.waitIfInternetNotAvailable(false);

            boolean initialized = false;
            for (final InetAddress localAddress : NetworkUtil.getLocalAddresses()) {
                try {
                    final String localNetworkIp = localAddress.getHostAddress();
                    final InetAddress localBindAddress = Addresses.asAddress(localNetworkIp);
                    final NetworkInterface localBindInterface = initJpcapNetworkInterface(localBindAddress);
                    for (final APacketCaptor c : captor) {
                        final JpcapCaptor pcapCaptor = JpcapCaptor.openDevice(localBindInterface, PCAP_SNAPLEN,
                                PCAP_PROMISC, PCAP_TO_MS);
                        c.initialize(pcapCaptor, localBindAddress);
                    }

                    final JpcapSender pcapSender = JpcapSender.openDevice(localBindInterface);
                    final DatalinkPacket datalink = initDatalinkPacket(localBindInterface);
                    sender.initialize(pcapSender, datalink, localBindAddress);
                    log.info("Using local address " + localBindAddress + " via interface "
                            + macToString(localBindInterface.mac_address) + ".");
                    initialized = true;
                    break;
                } catch (final TryNextInterfaceException e) {
                    for (final APacketCaptor c : captor) {
                        c.close();
                    }
                    continue;
                }
            }
            Assertions.assertThat(initialized).as("No NetworkInterface found with internet connection!").isTrue();

        } finally {
            synchronized (PortscanBootstrap.class) {
                initializing = false;
                PortscanBootstrap.class.notifyAll();
            }
        }
        //Check must happen after the initialization to prevent deadlock
        Assertions.assertThat(checker.icmpWorks())
                .as("Icmp check failed! The network autodetections seems to have failed! Please fix the problem and restart.")
                .isTrue();
        Assertions.assertThat(checker.synWorks())
                .as("Syn check faied! The network autodetections seems to have failed! Please fix the problem and restart.")
                .isTrue();
    }

    /****************************** private **************************/

    @Override
    public void shutdown() throws Exception {
        if (captor != null) {
            for (final APacketCaptor c : captor) {
                c.close();
            }
        }
        if (sender != null) {
            sender.close();
        }
    }

    /**
     * @see <a href="http://netresearch.ics.uci.edu/kfujii/jpcap/sample/ARP.java">Source</a>
     */
    private NetworkInterface initJpcapNetworkInterface(final InetAddress localBindAddress) {
        final NetworkInterface[] devices = JpcapCaptor.getDeviceList();
        NetworkInterface device = null;

        loop: for (final NetworkInterface d : devices) {
            for (final NetworkInterfaceAddress addr : d.addresses) {
                if (!(addr.address instanceof Inet4Address)) {
                    continue;
                }
                final byte[] bip = localBindAddress.getAddress();
                final byte[] subnet = addr.subnet.getAddress();
                final byte[] bif = addr.address.getAddress();
                for (int i = 0; i < 4; i++) {
                    bip[i] = (byte) (bip[i] & subnet[i]);
                    bif[i] = (byte) (bif[i] & subnet[i]);
                }
                if (Arrays.equals(bip, bif)) {
                    device = d;
                    break loop;
                }
            }
        }

        Assertions.assertThat(device)
                .as("No NetworkInteface found for IP [%s]! Has the application been started with root/admin rights?",
                        localBindAddress)
                .isNotNull();
        return device;
    }

    private DatalinkPacket initDatalinkPacket(final NetworkInterface localBindInterface)
            throws TryNextInterfaceException {
        final EthernetPacket datalink = new EthernetPacket();
        datalink.frametype = EthernetPacket.ETHERTYPE_IP;
        datalink.src_mac = localBindInterface.mac_address;
        datalink.dst_mac = initDefaultGatewayMac(localBindInterface);
        return datalink;
    }

    /**
     * <a href="http://netresearch.ics.uci.edu/kfujii/jpcap/sample/Traceroute.java">Source</a>
     */
    private byte[] initDefaultGatewayMac(final NetworkInterface localBindInterface) throws TryNextInterfaceException {
        try {
            final NetworkInterface device = localBindInterface;
            final JpcapCaptor captor = JpcapCaptor.openDevice(device, PCAP_SNAPLEN, PCAP_PROMISC, PCAP_TO_MS);
            //obtain MAC address of the default gateway
            final InetAddress pingAddr = PortscanProperties.CHECK_HOST;
            captor.setFilter("tcp and dst host " + pingAddr.getHostAddress(), true);
            EthernetPacket gwTrace = null;
            while (gwTrace == null) {
                URIs.asUrl("http://" + PortscanProperties.CHECK_HOST.getHostName()).openStream().close();
                final Packet ping = captor.getPacket();
                if (ping == null) {
                    throw new TryNextInterfaceException();
                }
                final EthernetPacket ether = (EthernetPacket) ping.datalink;
                if (!Arrays.equals(ether.dst_mac, device.mac_address)) {
                    gwTrace = ether;
                }
            }
            captor.close();
            log.info("Detected default gateway is " + gwTrace.getDestinationAddress() + ".");
            return gwTrace.dst_mac;
        } catch (final IOException e) {
            throw Err.process(e);
        }
    }

    private String macToString(final byte[] mac) {
        final EthernetPacket e = new EthernetPacket();
        e.dst_mac = mac;
        return e.getDestinationAddress();
    }

}
