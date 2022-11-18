package de.invesdwin.webproxy.broker.internal;

import java.util.Locale;
import java.util.TimeZone;

import javax.annotation.concurrent.ThreadSafe;
import jakarta.inject.Inject;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;

import de.invesdwin.context.integration.network.RandomIpGenerator;
import de.invesdwin.context.persistence.jpa.test.APersistenceTestPreparer;
import de.invesdwin.util.time.date.FDate;
import de.invesdwin.util.time.date.FTimeUnit;
import de.invesdwin.webproxy.broker.contract.BrokerContractProperties;
import de.invesdwin.webproxy.broker.contract.schema.ProxyQuality;
import de.invesdwin.webproxy.broker.contract.schema.ProxyType;
import de.invesdwin.webproxy.broker.internal.persistence.ProxyDao;
import de.invesdwin.webproxy.broker.internal.persistence.ProxyEntity;
import de.invesdwin.webproxy.broker.internal.persistence.RawProxyDao;
import de.invesdwin.webproxy.broker.internal.persistence.RawProxyEntity;

@ThreadSafe
@Configurable
public class BrokerServiceTestPreparer extends APersistenceTestPreparer {

    public static final int COUNT_PROXIES_DOWNTIME_TOLERANCE_EXCEEDED = BrokerContractProperties.MAX_PROXIES_PER_TASK
            / 2;
    public static final int COUNT_PROXIES = BrokerProperties.MAX_SPECIFIC_TO_BE_SCANNED_PORTS
            + COUNT_PROXIES_DOWNTIME_TOLERANCE_EXCEEDED;
    public static final int COUNT_PROXIES_WITH_DIFFERENT_PORTS = COUNT_PROXIES;
    public static final int COUNT_RAW_PROXIES = BrokerContractProperties.MAX_PROXIES_PER_TASK / 2;

    @Inject
    private ProxyDao proxyDao;
    @Inject
    private RawProxyDao rawProxyDao;

    @Override
    @Transactional
    public void prepare() {
        int port = 1;
        for (int i = 1; i <= COUNT_PROXIES; i++) {
            final String host = RandomIpGenerator.getRandomIp().getHostAddress();
            final ProxyType type;
            if (i % 2 == 0) {
                type = ProxyType.HTTP;
            } else {
                type = ProxyType.SOCKS;
            }
            final ProxyEntity p = ProxyEntity.valueOf(host, port, type, ProxyQuality.TRANSPARENT,
                    Locale.getDefault().getCountry(), TimeZone.getDefault().getID());
            if (i < COUNT_PROXIES_DOWNTIME_TOLERANCE_EXCEEDED) {
                p.setLastSuccessful(new FDate().addMilliseconds(
                        -BrokerProperties.PROXY_DOWNTIME_TOLERANCE.intValue(FTimeUnit.MILLISECONDS) * 2));
            } else {
                p.setLastSuccessful(new FDate());
            }

            if (!proxyDao.writeOrUpdate(p)) {
                i--;
            } else {
                port = getNextUniquePort(port);
            }
        }
        for (int i = 1; i < COUNT_PROXIES; i++) {
            final String host = RandomIpGenerator.getRandomIp().getHostAddress();
            final RawProxyEntity p = RawProxyEntity.valueOf(host, port);
            if (!rawProxyDao.writeIfNotExists(p)) {
                i--;
            } else {
                port = getNextUniquePort(port);
            }
        }
    }

    private int getNextUniquePort(final int currentPort) {
        int nextPort = currentPort + 1;
        while (true) {
            try {
                if (OftenUsedProxyPorts.valueOf(nextPort) != null) {
                    nextPort++;
                }
            } catch (final IllegalArgumentException e) {
                break;
            }
        }
        return nextPort;
    }

}
