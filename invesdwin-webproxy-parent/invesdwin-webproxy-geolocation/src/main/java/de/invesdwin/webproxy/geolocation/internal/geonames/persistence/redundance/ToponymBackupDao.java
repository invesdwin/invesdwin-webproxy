package de.invesdwin.webproxy.geolocation.internal.geonames.persistence.redundance;

import javax.annotation.concurrent.ThreadSafe;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import de.invesdwin.webproxy.geolocation.internal.geonames.persistence.AToponymDao;

@Named
@ThreadSafe
public class ToponymBackupDao extends AToponymDao<ToponymBackupEntity> {

    @Inject
    private ToponymDao toponymDao;

    @Override
    protected Class<ToponymBackupEntity> findGenericType() {
        return ToponymBackupEntity.class;
    }

    @Override
    protected AToponymDao<?> getOtherDao() {
        return toponymDao;
    }

}
