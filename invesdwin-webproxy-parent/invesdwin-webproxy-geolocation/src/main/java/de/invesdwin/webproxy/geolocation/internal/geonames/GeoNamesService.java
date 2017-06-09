package de.invesdwin.webproxy.geolocation.internal.geonames;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.scheduling.annotation.Scheduled;

import de.invesdwin.context.beans.hook.IStartupHook;
import de.invesdwin.context.integration.retry.RetryLaterException;
import de.invesdwin.webproxy.geolocation.internal.geonames.persistence.AToponymEntity;
import de.invesdwin.webproxy.geolocation.internal.geonames.persistence.redundance.ToponymRepository;

@ThreadSafe
@Named
public class GeoNamesService implements IGeoNamesService, IStartupHook {

    private volatile boolean updating;

    @Inject
    private ToponymRepository toponymRepo;
    @Inject
    private GeoNamesDataUpdater dataUpdater;

    @Override
    public AToponymEntity getToponym(final float latitude, final float longitude) throws RetryLaterException {
        /*
         * Throw exception if currently updating because inconsistent data could be used. Waiting here would take too
         * long.
         */
        if (updating) {
            throw new RetryLaterException(GeoNamesService.class.getSimpleName()
                    + " is currently importing its data initially.");
        }
        return toponymRepo.getActiveDao().findNearestNeighbour(latitude, longitude);
    }

    @Override
    public void startup() throws Exception {
        updateGeoNamesData();
    }

    /**
     * File is being updated every day on the server. We still only download it once every month because it is quite
     * large. This is still scheduled to be checked every day, so that the download/import can be retried on the next
     * day.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    private void updateGeoNamesData() throws Exception {
        try {
            if (toponymRepo.getActiveDao().isEmpty()) {
                //Requests should wait if there is no data there yet
                updating = true;
                dataUpdater.eventuallyUpdateData();
            } else {
                dataUpdater.eventuallyUpdateData();
            }
        } finally {
            updating = false;
        }
    }

}
