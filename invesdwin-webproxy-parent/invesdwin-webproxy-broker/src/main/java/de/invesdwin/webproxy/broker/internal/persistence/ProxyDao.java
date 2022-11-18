package de.invesdwin.webproxy.broker.internal.persistence;

import java.util.List;

import javax.annotation.concurrent.ThreadSafe;
import jakarta.inject.Named;

import org.springframework.transaction.annotation.Transactional;

import de.invesdwin.context.persistence.jpa.api.dao.ADao;
import de.invesdwin.util.time.date.FDate;
import de.invesdwin.webproxy.broker.contract.schema.RawProxy;
import de.invesdwin.webproxy.broker.internal.BrokerProperties;
import jakarta.persistence.Query;
import jakarta.persistence.TemporalType;

@Named
@ThreadSafe
public class ProxyDao extends ADao<ProxyEntity> {

    /**
     * Returns all saved ports descending sorted by frequentness.
     */
    @SuppressWarnings("unchecked")
    public List<Integer> readUsedPortsSorted(final int maxResults) {
        return getEntityManager()
                .createQuery("SELECT " + ProxyEntity_.port.getName() + " FROM " + ProxyEntity.class.getName()
                        + " GROUP BY " + ProxyEntity_.port.getName() + " ORDER BY COUNT(" + ProxyEntity_.port.getName()
                        + ") DESC")
                .setMaxResults(maxResults)
                .getResultList();
    }

    /**
     * To circumvent unique constraint exceptions.
     */
    @Transactional
    public synchronized boolean writeOrUpdate(final ProxyEntity proxy) {
        final ProxyEntity example = new ProxyEntity();
        example.setHost(proxy.getHost());
        example.setPort(proxy.getPort());

        final ProxyEntity alreadyExists = findOne(example);
        if (alreadyExists == null) {
            if (proxy.getLastSuccessful() == null) {
                proxy.setLastSuccessful(new FDate());
            }
            save(proxy);
            return true;
        } else {
            alreadyExists.mergeFrom(proxy);
            alreadyExists.setLastSuccessful(new FDate());
            save(alreadyExists);
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public List<ProxyEntity> readDowntimeToleranceExceededProxies() {
        final String paramExceeded = "exceeded";
        final FDate expired = BrokerProperties.calculateDowntimeToleranceExceededDate();
        return getEntityManager()
                .createQuery("SELECT e FROM " + ProxyEntity.class.getName() + " e WHERE e."
                        + ProxyEntity_.lastSuccessful.getName() + " < :" + paramExceeded)
                .setParameter(paramExceeded, expired.dateValue(), TemporalType.TIMESTAMP)
                .getResultList();
    }

    public boolean shouldRawProxyStillBeVerified(final RawProxy rawProxy) {
        final String paramExceeded = "exceeded";
        final String paramHost = "host";
        final FDate exceeded = BrokerProperties.calculateDowntimeToleranceExceededDate();

        final String paramPort = "port";
        String eventuallyAndPortEqualsParamPort = "";
        if (rawProxy.getPort() != null) {
            eventuallyAndPortEqualsParamPort = " AND " + ProxyEntity_.port.getName() + " = :" + paramPort;
        }

        final Query query = getEntityManager()
                .createQuery("SELECT count(*) FROM " + ProxyEntity.class.getName() + " WHERE "
                        + ProxyEntity_.host.getName() + " = :" + paramHost + eventuallyAndPortEqualsParamPort + " AND "
                        + ProxyEntity_.lastSuccessful.getName() + " > :" + paramExceeded)
                .setParameter(paramHost, rawProxy.getHost())
                .setParameter(paramExceeded, exceeded.dateValue(), TemporalType.TIMESTAMP);

        if (eventuallyAndPortEqualsParamPort.length() > 0) {
            query.setParameter(paramPort, rawProxy.getPort());
        }

        final Long count = (Long) query.getSingleResult();
        return count == 0;
    }

    @Transactional
    public void deleteIfDowntimeToleranceExceeded(final RawProxy rawProxy) {
        final String paramExceeded = "exceeded";
        final String paramHost = "host";
        final String paramPort = "port";
        final FDate exceeded = BrokerProperties.calculateDowntimeToleranceExceededDate();
        getEntityManager()
                .createQuery("DELETE FROM " + ProxyEntity.class.getName() + " WHERE " + ProxyEntity_.host.getName()
                        + " = :" + paramHost + " AND " + ProxyEntity_.port.getName() + " = :" + paramPort + " AND "
                        + ProxyEntity_.lastSuccessful.getName() + " < :" + paramExceeded)
                .setParameter(paramHost, rawProxy.getHost())
                .setParameter(paramPort, rawProxy.getPort())
                .setParameter(paramExceeded, exceeded.dateValue(), TemporalType.TIMESTAMP)
                .executeUpdate();
    }

}
