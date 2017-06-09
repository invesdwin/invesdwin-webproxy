package de.invesdwin.webproxy.portscan.internal.pcap.icmp;

import java.net.InetAddress;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.concurrent.ThreadSafe;

import de.invesdwin.util.time.duration.Duration;
import de.invesdwin.webproxy.portscan.internal.PortscanProperties;
import de.invesdwin.webproxy.portscan.internal.pcap.AScanTracker;

@ThreadSafe
public class IcmpScanTracker extends AScanTracker {
    public static final Duration RESPONSE_TIMEOUT = PortscanProperties.ICMP_RESPONSE_TIMEOUT;

    private static final long serialVersionUID = 1L;
    private static final Object LOCK = new Object();
    private static final AtomicInteger OPEN_REQUESTS_COUNTER = new AtomicInteger();
    private final Set<IPingListener> listeners = new CopyOnWriteArraySet<IPingListener>();

    public IcmpScanTracker(final IPingListener listener, final InetAddress host) {
        super(host, LOCK, OPEN_REQUESTS_COUNTER, PortscanProperties.MAX_OPEN_ICMP_REQUESTS, RESPONSE_TIMEOUT);
        listeners.add(listener);
    }

    public Set<IPingListener> getListeners() {
        return listeners;
    }

    public boolean addListener(final IPingListener listener) {
        return listeners.add(listener);
    }

}
