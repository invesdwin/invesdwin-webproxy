package de.invesdwin.webproxy.crawler.verification;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import de.invesdwin.context.log.Log;
import de.invesdwin.util.collections.Collections;
import de.invesdwin.util.collections.Iterables;
import de.invesdwin.util.collections.list.Lists;
import de.invesdwin.util.lang.uri.Addresses;
import de.invesdwin.webproxy.broker.contract.BrokerContractProperties;
import de.invesdwin.webproxy.broker.contract.ProxyUtil;
import de.invesdwin.webproxy.broker.contract.schema.RawProxy;
import de.invesdwin.webproxy.crawler.CrawlerProperties;
import de.invesdwin.webproxy.portscan.contract.IPortscanClient;
import de.invesdwin.webproxy.portscan.contract.IPortscanService;
import de.invesdwin.webproxy.portscan.contract.schema.PortscanAsyncRequest.RandomScanRequest;
import de.invesdwin.webproxy.portscan.contract.schema.PortscanAsyncRequest.ScanRequest;
import de.invesdwin.webproxy.portscan.contract.schema.PortscanAsyncResponse.PingResponse;
import de.invesdwin.webproxy.portscan.contract.schema.PortscanAsyncResponse.ScanResponse;
import de.invesdwin.webproxy.portscan.contract.schema.PortscanSyncResponse.StatusResponse;
import de.invesdwin.webproxy.portscan.contract.schema.RandomScan;

@Named
@ThreadSafe
public class TaskAcquirerCache implements IPortscanClient {

    @GuardedBy("this")
    private final Set<RawProxy> cachedVerifications = new HashSet<RawProxy>();
    @GuardedBy("this")
    private final Set<InetAddress> cachedPortscans = new HashSet<InetAddress>();
    private final Log log = new Log(this);
    private volatile List<Integer> toBeScannedPorts = Collections.unmodifiableList(new ArrayList<Integer>());

    @Inject
    private ProxyVerifier verifier;
    @Inject
    private IPortscanService portscan;

    public void setToBeScannedPorts(final List<Integer> toBeScannedPorts) {
        this.toBeScannedPorts = toBeScannedPorts;
    }

    public List<Integer> getToBeScannedPorts() {
        return toBeScannedPorts;
    }

    @Override
    public synchronized void hostIsReachable(final PingResponse response) {
        //If the ping was successful, we can do a portscan on the host
        cachedPortscans.add(Addresses.asAddress(response.getRespondingHost()));
    }

    @Override
    public synchronized void portIsReachable(final ScanResponse response) {
        //When a responding port on a host was found, we can verify if there is a proxy behind it
        final RawProxy potenziellerProxy = ProxyUtil.valueOf(response.getScannedHost(), response.getRespondingPort());
        cachedVerifications.add(potenziellerProxy);
    }

    public void waitTillPortscannerIsIdle() throws InterruptedException {
        StatusResponse status = null;
        boolean logOnceWhenProcessingTasks = true;
        boolean waitNecessary = false;
        do {
            status = portscan.status();
            if (status.isRandomScanActive()) {
                final RandomScanRequest request = new RandomScanRequest();
                request.setStartOrStop(RandomScan.STOP);
                portscan.randomScan(request);
                log.info("Gave portscanner the signal to stop the random scan");
            }
            waitNecessary = status.isRandomScanActive()
                    || (status.isRequestProcessingActive() && CrawlerProperties.WAIT_FOR_PORTSCAN_PROCESSING_END);
            if (waitNecessary) {
                if (logOnceWhenProcessingTasks) {
                    log.info("Waiting until the portscanner is done with processing its tasks");
                    logOnceWhenProcessingTasks = false;
                }
                TimeUnit.MINUTES.sleep(1);
            }
        } while (waitNecessary);
    }

    public synchronized boolean isCacheReadyForProcessing(final boolean restProcessing) {
        final boolean cachedVerificationsFull = cachedVerifications
                .size() >= BrokerContractProperties.MAX_PROXIES_PER_TASK;
        final boolean cachedPortscansFull = cachedPortscans.size() >= BrokerContractProperties.MAX_PROXIES_PER_TASK;
        final boolean restsToBeProcessed = restProcessing
                && (cachedVerifications.size() > 0 || cachedPortscans.size() > 0);
        return cachedVerificationsFull || cachedPortscansFull || restsToBeProcessed;
    }

    /**
     * Processing occurs in packets, so that never more than the allowed cache size of the broker gets processed.
     */
    public void processCache() throws InterruptedException {
        while (isCacheReadyForProcessing(true)) {
            final Set<RawProxy> verifications;
            final Set<InetAddress> portscans;

            synchronized (this) {
                verifications = new HashSet<RawProxy>(Lists.newArrayList(
                        Iterables.limit(cachedVerifications, BrokerContractProperties.MAX_PROXIES_PER_TASK)));
                cachedVerifications.removeAll(verifications);
                portscans = new HashSet<InetAddress>(cachedPortscans);
                cachedPortscans.clear();
            }

            if (portscans.size() > 0) {
                final List<Integer> toBeScannedPorts = getToBeScannedPorts();
                log.info("Got from %s hosts a ping response, now running a portscan on then over %s ports",
                        portscans.size(), toBeScannedPorts.size());
                for (final InetAddress host : portscans) {
                    final ScanRequest request = new ScanRequest();
                    request.setToBeScannedHost(host.getHostAddress());
                    request.getToBeScannedPorts().addAll(toBeScannedPorts);
                    portscan.scan(request);
                }
                portscans.clear();
            }
            waitTillPortscannerIsIdle();
            if (verifications.size() > 0) {
                log.info("Starting with the verification of %s possible proxies", verifications.size());
                verifier.verify(verifications);
            }
        }
    }

}
