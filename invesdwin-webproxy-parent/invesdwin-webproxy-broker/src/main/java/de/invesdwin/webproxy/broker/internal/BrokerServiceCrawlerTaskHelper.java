package de.invesdwin.webproxy.broker.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.springframework.scheduling.annotation.Scheduled;

import de.invesdwin.context.beans.hook.IStartupHook;
import de.invesdwin.context.persistence.jpa.api.query.QueryConfig;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.lang.uri.Addresses;
import de.invesdwin.webproxy.broker.contract.BrokerContractProperties;
import de.invesdwin.webproxy.broker.contract.schema.BrokerResponse.GetTaskForCrawlerResponse;
import de.invesdwin.webproxy.broker.contract.schema.RawProxy;
import de.invesdwin.webproxy.broker.internal.persistence.ProxyDao;
import de.invesdwin.webproxy.broker.internal.persistence.ProxyEntity;
import de.invesdwin.webproxy.broker.internal.persistence.RawProxyDao;
import de.invesdwin.webproxy.broker.internal.persistence.RawProxyEntity;

@Named
@ThreadSafe
public class BrokerServiceCrawlerTaskHelper implements IStartupHook {

    @GuardedBy("this")
    private boolean proxyCrawlingTriggered;

    @Inject
    private ProxyDao proxyDao;
    @Inject
    private RawProxyDao rawProxyDao;

    public GetTaskForCrawlerResponse getTaskForCrawler() {
        final GetTaskForCrawlerResponse response = new GetTaskForCrawlerResponse();
        synchronized (this) {
            //only one crawler will get this task at the time is needs to be done
            response.setCrawlForProxies(proxyCrawlingTriggered);
            proxyCrawlingTriggered = false;
        }
        response.getToBeScannedPorts().addAll(getToBeScannedPortsSorted());
        if (!response.isCrawlForProxies()) {
            response.getToBeVerifiedProxies().addAll(getZuPruefendeProxies());
        }
        return response;
    }

    private Collection<Integer> getToBeScannedPortsSorted() {
        final Set<Integer> ports = new LinkedHashSet<Integer>();
        //the enum ports have the highest priority
        for (final OftenUsedProxyPorts p : OftenUsedProxyPorts.values()) {
            ports.add(p.toPort());
        }
        //after that the ports from the database are checked
        ports.addAll(proxyDao.readUsedPortsSorted(BrokerProperties.MAX_SPECIFIC_TO_BE_SCANNED_PORTS - ports.size()));
        //keep the limit and enrich it with random ports
        return enhanceWithRandomPorts(ports);
    }

    private Collection<Integer> enhanceWithRandomPorts(final Set<Integer> ports) {
        //negation of the to be scanned ports so that the random ports can be picked from that
        final List<Integer> randomScannablePorts = new ArrayList<Integer>(Addresses.ALL_PORTS);
        Assertions.assertThat(randomScannablePorts.removeAll(ports)).isTrue();
        //random ports are being calculated before hand, this saves time by preventing unneeded iterations
        int randomPorts = BrokerProperties.calculateAdditionalRandomToBeScannedPorts(ports.size());
        final RandomDataGenerator randomData = new RandomDataGenerator();
        while (randomPorts > 0 && randomScannablePorts.size() > 0) {
            final int randomIndex = randomData.nextInt(0, randomScannablePorts.size() - 1);
            final int selectedPort = randomScannablePorts.remove(randomIndex);
            ports.add(selectedPort);
            randomPorts--;
        }
        return ports;
    }

    private Collection<RawProxy> getZuPruefendeProxies() {
        final Set<RawProxy> ret = new HashSet<RawProxy>();
        List<RawProxyEntity> rawProxyEnts;
        do {
            rawProxyEnts = rawProxyDao.findAll(new QueryConfig().setMaxResults(BrokerContractProperties.MAX_PROXIES_PER_TASK));
            for (final RawProxyEntity rawProxyEnt : rawProxyEnts) {
                if (ret.size() >= BrokerContractProperties.MAX_PROXIES_PER_TASK) {
                    break;
                }
                //transmitted tasks get removed from DB
                rawProxyDao.delete(rawProxyEnt);
                final RawProxy rawProxy = rawProxyEnt.toRawProxy();
                //in the meantime a proxy could aready have been verified successfully, thus we check this here
                if (proxyDao.shouldRawProxyStillBeVerified(rawProxy)) {
                    ret.add(rawProxy);
                }
            }
        } while (ret.size() < BrokerContractProperties.MAX_PROXIES_PER_TASK && rawProxyEnts.size() > 0);
        return ret;
    }

    /**
     * Once every day
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void triggerProxyCrawling() {
        searchForExpiredProxies();
        synchronized (this) {
            proxyCrawlingTriggered = true;
        }
    }

    private void searchForExpiredProxies() {
        //If slots for tasks are still free, we fill them with proxies that haven't been verified since some time
        for (final ProxyEntity proxyEnt : proxyDao.readDowntimeToleranceExceededProxies()) {
            rawProxyDao.writeIfNotExists(RawProxyEntity.valueOf(proxyEnt));
        }
    }

    @Override
    public void startup() throws Exception {
        if (rawProxyDao.count() < 1000) {
            triggerProxyCrawling();
        } else {
            searchForExpiredProxies();
        }

    }

}
