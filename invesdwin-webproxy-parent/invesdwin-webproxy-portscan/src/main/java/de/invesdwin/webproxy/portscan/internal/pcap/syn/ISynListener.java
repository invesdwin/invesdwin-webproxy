package de.invesdwin.webproxy.portscan.internal.pcap.syn;

import java.net.InetAddress;

public interface ISynListener {

    void synAckReceived(InetAddress host, int port);

}
