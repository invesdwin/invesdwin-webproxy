package de.invesdwin.webproxy.broker.internal.persistence;

import javax.annotation.concurrent.ThreadSafe;
import jakarta.inject.Named;

import org.springframework.transaction.annotation.Transactional;

import de.invesdwin.context.persistence.jpa.api.dao.ADao;

@ThreadSafe
@Named
public class RawProxyDao extends ADao<RawProxyEntity> {

    /**
     * To circumvent Unique Constraint exceptions.
     */
    @Transactional
    public synchronized boolean writeIfNotExists(final RawProxyEntity rawProxy) {
        final RawProxyEntity example = new RawProxyEntity();
        example.setHost(rawProxy.getHost());
        example.setPort(rawProxy.getPort());

        if (!exists(example)) {
            save(rawProxy);
            return true;
        } else {
            return false;
        }
    }

}
