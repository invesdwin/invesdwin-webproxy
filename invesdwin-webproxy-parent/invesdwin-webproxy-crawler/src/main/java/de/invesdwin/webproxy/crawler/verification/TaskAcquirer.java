package de.invesdwin.webproxy.crawler.verification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import javax.inject.Named;

import de.invesdwin.context.beans.hook.IStartupHook;
import de.invesdwin.context.log.Log;
import de.invesdwin.util.concurrent.Executors;
import de.invesdwin.util.concurrent.Threads;
import de.invesdwin.util.concurrent.WrappedExecutorService;
import de.invesdwin.util.shutdown.IShutdownHook;
import de.invesdwin.util.time.Instant;
import de.invesdwin.util.time.duration.Duration;
import de.invesdwin.util.time.fdate.FTimeUnit;
import de.invesdwin.webproxy.broker.contract.IBrokerService;
import de.invesdwin.webproxy.broker.contract.schema.BrokerResponse.GetTaskForCrawlerResponse;
import de.invesdwin.webproxy.broker.contract.schema.RawProxy;
import de.invesdwin.webproxy.crawler.CrawlerProperties;
import de.invesdwin.webproxy.crawler.sources.ProxyCrawlerSources;
import de.invesdwin.webproxy.portscan.contract.IPortscanClient;
import de.invesdwin.webproxy.portscan.contract.IPortscanService;
import de.invesdwin.webproxy.portscan.contract.schema.PortscanAsyncRequest.PingRequest;
import de.invesdwin.webproxy.portscan.contract.schema.PortscanAsyncRequest.RandomScanRequest;
import de.invesdwin.webproxy.portscan.contract.schema.PortscanAsyncResponse.ScanResponse;
import de.invesdwin.webproxy.portscan.contract.schema.RandomScan;

@ThreadSafe
@Named
public class TaskAcquirer implements IStartupHook, IShutdownHook {

    private final Log log = new Log(this);

    private final WrappedExecutorService executor = Executors.newFixedThreadPool(TaskAcquirer.class.getSimpleName(), 1);

    @Inject
    private IBrokerService broker;
    @Inject
    private IPortscanService portscan;
    @Inject
    private ProxyCrawlerSources crawler;
    @Inject
    private IPortscanClient client;
    @Inject
    private TaskAcquirerCache cache;

    @Override
    public void startup() throws Exception {
        executor.execute(new TaskCollector());
    }

    @Override
    public void shutdown() {
        final RandomScanRequest request = new RandomScanRequest();
        request.setStartOrStop(RandomScan.STOP);
        portscan.randomScan(request);
    }

    private class TaskCollector implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    boolean restProcessingAllowed = false;
                    cache.waitTillPortscannerIsIdle(); //if random scan ist still running, stop it fist
                    final GetTaskForCrawlerResponse response = broker.getTaskForCrawler();
                    cache.setToBeScannedPorts(Collections.unmodifiableList(response.getToBeScannedPorts()));
                    Duration restartDelay = null;
                    if (response.isCrawlForProxies()) {
                        crawlProxies();
                    }
                    //checking raw proxies has a higher priority than doing random scans
                    final List<RawProxy> toBeVerifiedProxies = response.getToBeVerifiedProxies();
                    if (toBeVerifiedProxies.size() > 0) {
                        verifyProxies(toBeVerifiedProxies);
                    } else if (!response.isCrawlForProxies()) {
                        if (CrawlerProperties.RANDOM_SCAN_ALLOWED) {
                            randomScan();
                        } else {
                            restProcessingAllowed = true;
                        }
                        restartDelay = CrawlerProperties.MAX_RANDOM_SCAN_DURATION;
                    }
                    waitForTasks(restartDelay, restProcessingAllowed);
                }
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        private void crawlProxies() throws InterruptedException {
            log.info("Got the task to crawl for proxies");
            crawler.crawlProxies();
        }

        private void verifyProxies(final List<RawProxy> toBeVerifiedProxies) throws InterruptedException {
            final List<RawProxy> proxiesWithoutPort = new ArrayList<RawProxy>();
            final List<RawProxy> proxiesWithPort = new ArrayList<RawProxy>();
            //first check the proxies port by SYN-Scan
            while (toBeVerifiedProxies.size() > 0) {
                final RawProxy possibleProxy = toBeVerifiedProxies.get(0);
                if (possibleProxy.getPort() == null) {
                    proxiesWithoutPort.add(possibleProxy);
                } else {
                    proxiesWithPort.add(possibleProxy);
                }
                //Now we can be sure, that the task will be worked on
                toBeVerifiedProxies.remove(possibleProxy);
            }
            if (log.isInfoEnabled()) {
                final StringBuilder info = new StringBuilder("Got the task to verify ");
                info.append(proxiesWithoutPort.size() + proxiesWithPort.size());
                info.append(" possible proxies.");
                if (proxiesWithPort.size() > 0) {
                    info.append(" ");
                    info.append(proxiesWithPort.size());
                    info.append(" can be verified directly.");
                }
                if (proxiesWithoutPort.size() > 0) {
                    info.append(" Other ");
                    info.append(proxiesWithoutPort.size());
                    info.append(" hosts first need a portscan.");
                }
                log.info(info.toString());
            }
            for (final RawProxy proxyWithoutPort : proxiesWithoutPort) {
                final PingRequest pingRequest = new PingRequest();
                pingRequest.setToBePingedHost(proxyWithoutPort.getHost());
                portscan.ping(pingRequest);
            }
            for (final RawProxy proxyWithPort : proxiesWithPort) {
                /*
                 * Here we cannot first give this to the portscanner for verification, because without a response we
                 * wouldn't be able to give back a result to the broker so that a non working proxy can be deleted from
                 * the working proxy list.
                 */
                final ScanResponse fakeResponse = new ScanResponse();
                fakeResponse.setScannedHost(proxyWithPort.getHost());
                fakeResponse.setRespondingPort(proxyWithPort.getPort());
                client.portIsReachable(fakeResponse);
            }
        }

        private void randomScan() throws InterruptedException {
            log.info(
                    "Don't have anything else to do, thus giving portscanner the signal to start the random scan. Using %s ports for this",
                    cache.getToBeScannedPorts().size());
            final RandomScanRequest randomScanRequest = new RandomScanRequest();
            randomScanRequest.setStartOrStop(RandomScan.START);
            randomScanRequest.getToBeScannedPorts().addAll(cache.getToBeScannedPorts());
            portscan.randomScan(randomScanRequest);
        }

        private void waitForTasks(final Duration delay, final boolean restProcessingAllowed)
                throws InterruptedException {
            if (delay != null) {
                final Instant delayStart = new Instant();
                boolean logOnce = true;
                while (new Duration(delayStart).isLessThan(CrawlerProperties.MAX_RANDOM_SCAN_DURATION)
                        && !cache.isCacheReadyForProcessing(restProcessingAllowed)) {
                    Threads.throwIfInterrupted();
                    if (logOnce) {
                        logOnce = false;
                        log.info("Now sleeping for a maximum of %s", CrawlerProperties.MAX_RANDOM_SCAN_DURATION);
                    }
                    TimeUnit.MINUTES.sleep(CrawlerProperties.MAX_RANDOM_SCAN_DURATION.longValue(FTimeUnit.MINUTES) / 6);
                }
            }
            if (cache.isCacheReadyForProcessing(restProcessingAllowed)) {
                cache.processCache();
            }
        }
    }

}
