package de.invesdwin.webproxy.portscan.internal;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.ThreadSafe;

import de.invesdwin.util.collections.Collections;
import de.invesdwin.util.lang.uri.Addresses;
import de.invesdwin.util.math.random.IRandomGenerator;
import de.invesdwin.util.math.random.PseudoRandomGenerators;
import de.invesdwin.webproxy.portscan.contract.IPortscanClient;
import de.invesdwin.webproxy.portscan.contract.IPortscanService;
import de.invesdwin.webproxy.portscan.contract.schema.PortscanAsyncRequest.PingRequest;
import de.invesdwin.webproxy.portscan.contract.schema.PortscanAsyncRequest.RandomScanRequest;
import de.invesdwin.webproxy.portscan.contract.schema.PortscanAsyncRequest.ScanRequest;
import de.invesdwin.webproxy.portscan.contract.schema.PortscanAsyncResponse.PingResponse;
import de.invesdwin.webproxy.portscan.contract.schema.PortscanAsyncResponse.ScanResponse;
import de.invesdwin.webproxy.portscan.contract.schema.PortscanSyncResponse.StatusResponse;
import de.invesdwin.webproxy.portscan.contract.schema.RandomScan;
import de.invesdwin.webproxy.portscan.internal.pcap.icmp.IPingListener;
import de.invesdwin.webproxy.portscan.internal.pcap.icmp.IcmpCaptor;
import de.invesdwin.webproxy.portscan.internal.pcap.icmp.IcmpScanTracker;
import de.invesdwin.webproxy.portscan.internal.pcap.icmp.IcmpSender;
import de.invesdwin.webproxy.portscan.internal.pcap.syn.ISynListener;
import de.invesdwin.webproxy.portscan.internal.scanner.PortScanner;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@ThreadSafe
public class PortscanService implements IPortscanService, ISynListener, IPingListener {

    @Inject
    private PortScanner portScanner;
    @Inject
    private IcmpCaptor icmpCaptor;
    @Inject
    private IcmpSender icmpSender;
    @Inject
    private IPortscanClient portscanClient;

    @Override
    public void synAckReceived(final InetAddress host, final int port) {
        final ScanResponse response = new ScanResponse();
        response.setScannedHost(host.getHostAddress());
        response.setRespondingPort(port);
        portscanClient.portIsReachable(response);
    }

    @Override
    public void pingReceived(final InetAddress host) {
        final PingResponse response = new PingResponse();
        response.setRespondingHost(host.getHostAddress());
        portscanClient.hostIsReachable(response);
    }

    private List<Integer> randomizePorts(final List<Integer> requestPorts) {
        final List<Integer> quellPorts;
        if (requestPorts.size() > 0) {
            quellPorts = new ArrayList<Integer>(requestPorts);
        } else {
            quellPorts = new ArrayList<Integer>(Addresses.getAllPorts());
        }
        final List<Integer> randomizedPorts = new ArrayList<Integer>();
        final IRandomGenerator randomData = PseudoRandomGenerators.getThreadLocalPseudoRandom();
        while (quellPorts.size() > 0) {
            final int index;
            if (quellPorts.size() - 1 > 0) {
                index = randomData.nextInt(0, quellPorts.size() - 1);
            } else {
                index = 0;
            }
            randomizedPorts.add(quellPorts.remove(index));
        }
        return Collections.unmodifiableList(randomizedPorts);
    }

    /******************* service interface ******************/

    @Override
    public void ping(final PingRequest request) {
        final InetAddress host = Addresses.asAddress(request.getToBePingedHost());
        final IcmpScanTracker tracker = icmpCaptor.startTracking(this, host);
        try {
            icmpSender.sendIcmp(tracker);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void randomScan(final RandomScanRequest request) {
        if (request.getStartOrStop() == RandomScan.START) {
            final List<Integer> ports = randomizePorts(request.getToBeScannedPorts());
            portScanner.startRandomScan(this, ports);
        } else {
            portScanner.stopRandomScan();
        }
    }

    @Override
    public void scan(final ScanRequest request) {
        final InetAddress host = Addresses.asAddress(request.getToBeScannedHost());
        final List<Integer> ports = randomizePorts(request.getToBeScannedPorts());
        try {
            portScanner.portScan(this, host, ports);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public StatusResponse status() {
        final StatusResponse response = new StatusResponse();
        response.setRandomScanActive(portScanner.isRandomScanRunning());
        response.setRequestProcessingActive(portScanner.getActiveScans() > 0);
        return response;
    }

}
