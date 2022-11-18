package de.invesdwin.webproxy.portscan.internal.pcap.syn;

import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.concurrent.ThreadSafe;
import jakarta.inject.Named;

import de.invesdwin.webproxy.portscan.internal.PortscanProperties;

@Named
@ThreadSafe
public class SynScanQueue extends LinkedBlockingQueue<SynScanTracker> {

    private static final long serialVersionUID = 1L;

    public SynScanQueue() {
        super(PortscanProperties.MAX_PACKETS_PER_SECOND);
    }

}
