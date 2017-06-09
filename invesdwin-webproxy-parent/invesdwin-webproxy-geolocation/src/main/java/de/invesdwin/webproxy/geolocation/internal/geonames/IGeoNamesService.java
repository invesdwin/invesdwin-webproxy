package de.invesdwin.webproxy.geolocation.internal.geonames;

import de.invesdwin.context.integration.retry.RetryLaterException;
import de.invesdwin.webproxy.geolocation.internal.geonames.persistence.AToponymEntity;

public interface IGeoNamesService {

    AToponymEntity getToponym(float breitengrad, float laengengrad) throws RetryLaterException;

}
