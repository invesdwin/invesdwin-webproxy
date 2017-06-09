package de.invesdwin.webproxy.geolocation.internal.geonames.persistence.redundance;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import javax.inject.Named;

import de.invesdwin.webproxy.geolocation.internal.geonames.persistence.AToponymDao;

@ThreadSafe
@Named
public class ToponymDao extends AToponymDao<ToponymEntity> {

    @Inject
    private ToponymBackupDao toponymBackupDao;

    @Override
    protected Class<ToponymEntity> findGenericType() {
        return ToponymEntity.class;
    }

    @Override
    protected AToponymDao<?> getOtherDao() {
        return toponymBackupDao;
    }

}
