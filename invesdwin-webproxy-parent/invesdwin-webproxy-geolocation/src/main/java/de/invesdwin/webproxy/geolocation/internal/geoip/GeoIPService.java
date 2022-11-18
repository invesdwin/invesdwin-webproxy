package de.invesdwin.webproxy.geolocation.internal.geoip;

import java.io.File;
import java.io.IOException;

import javax.annotation.concurrent.ThreadSafe;
import jakarta.inject.Named;

import org.springframework.scheduling.annotation.Scheduled;

import com.maxmind.geoip.Location;
import com.maxmind.geoip.LookupService;

import de.invesdwin.context.ContextProperties;
import de.invesdwin.context.beans.hook.IStartupHook;
import de.invesdwin.context.integration.retry.RetryLaterException;
import de.invesdwin.context.log.error.Err;
import de.invesdwin.webproxy.geolocation.internal.ADataUpdater;
import de.invesdwin.webproxy.geolocation.internal.GeolocationProperties;

@ThreadSafe
@Named
public class GeoIPService extends ADataUpdater implements IGeoIPService, IStartupHook {

    private final File geoipDataFile = new File(ContextProperties.getCacheDirectory(),
            getClass().getSimpleName() + "_GeoIP.dat");

    private volatile boolean updating;

    @Override
    public synchronized Location getLocation(final String ip) throws RetryLaterException {
        /*
         * Throw exception if currently updating because inconsistent data could be used. Waiting here would take too
         * long.
         */
        if (updating) {
            throw new RetryLaterException(
                    GeoIPService.class.getSimpleName() + " is currently downloading its data initially.");
        }
        LookupService lookupService = null;
        try {
            lookupService = new LookupService(geoipDataFile, LookupService.GEOIP_STANDARD);
            Location loc = lookupService.getLocation(ip);
            if (loc == null) {
                log.warn("No result for [%s] available! Using the geographical center of the earth (0,0) per default.",
                        ip);
                loc = new Location();
                loc.longitude = 0;
                loc.latitude = 0;
            }
            return loc;
        } catch (final IOException e) {
            throw Err.process(e);
        } finally {
            if (lookupService != null) {
                lookupService.close();
            }
        }
    }

    @Override
    public void startup() throws Exception {
        updateGeoIPData();
    }

    /**
     * File gets updated on the first day of every month. We still check this daily and download it only if it is more
     * up to date than the current one.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    private void updateGeoIPData() throws IOException {
        try {
            if (!targetFileAlreadyExists(geoipDataFile)) {
                //Requests should retry later if the file does not exist yet
                updating = true;
                eventuallyUpdateData(geoipDataFile);
            } else {
                eventuallyUpdateData(geoipDataFile);
            }
        } finally {
            updating = false;
        }
    }

    private void eventuallyUpdateData(final File targetFile) throws IOException {
        super.eventuallyUpdateData(GeolocationProperties.GEOIP_DATA_URL, targetFile);
    }

    /**
     * Anfragen sollen warten, während die Datei ersetzt wird.
     */
    @Override
    protected void replaceFile(final File targetFile, final File tempZielDatei) throws IOException {
        updating = true;
        super.replaceFile(targetFile, tempZielDatei);
    }
}
