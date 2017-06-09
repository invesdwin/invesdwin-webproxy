package de.invesdwin.webproxy.portscan.internal.pcap.icmp;

import java.net.InetAddress;

public interface IPingListener {

    void pingReceived(InetAddress host);

}
