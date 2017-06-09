package de.invesdwin.webproxy.geolocation.internal.geonames.persistence.redundance;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.InitializingBean;

import de.invesdwin.webproxy.geolocation.internal.geonames.persistence.AToponymDao;
import de.invesdwin.webproxy.geolocation.internal.geonames.persistence.AToponymEntity;

@Named
@ThreadSafe
public class ToponymRepository implements InitializingBean {

    @Inject
    private ToponymDao dao;
    @Inject
    private ToponymBackupDao backupDao;

    @GuardedBy("this")
    private AToponymDao activeDao;
    @GuardedBy("this")
    private AToponymDao inactiveDao;

    public synchronized AToponymDao getActiveDao() {
        return activeDao;
    }

    public synchronized AToponymDao getInactiveDao() {
        return inactiveDao;
    }

    public synchronized AToponymEntity newActiveToponymEntity() {
        if (getActiveDao() == dao) {
            return new ToponymEntity();
        } else {
            return new ToponymBackupEntity();
        }
    }

    public synchronized AToponymEntity newInactiveToponymEntity() {
        if (getInactiveDao() == dao) {
            return new ToponymEntity();
        } else {
            return new ToponymBackupEntity();
        }
    }

    public synchronized void switchInactiveAndActiveDaoWithEachother() {
        final AToponymDao previousActiveDao = activeDao;
        activeDao = inactiveDao;
        inactiveDao = previousActiveDao;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        synchronized (this) {
            /*
             * Incomplete updates should be ignored, thats why the count instead of the timestamp is used to detect
             * incomplete updates
             */
            final boolean daoEmpty = dao.isEmpty();
            final boolean backupDaoEmpty = backupDao.isEmpty();
            if (!daoEmpty && backupDaoEmpty || (!daoEmpty && !backupDaoEmpty && dao.count() > backupDao.count())) {
                activeDao = dao;
                inactiveDao = backupDao;
            } else {
                activeDao = backupDao;
                inactiveDao = dao;
            }
        }
        if (!inactiveDao.isEmpty()) {
            //cleanup inactive dao
            inactiveDao.deleteAll();
        }
    }

}
