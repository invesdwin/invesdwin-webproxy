package de.invesdwin.webproxy.geolocation.internal.geonames.persistence.redundance;

import javax.annotation.concurrent.NotThreadSafe;
import javax.persistence.Entity;

import de.invesdwin.webproxy.geolocation.internal.geonames.persistence.AToponymEntity;

@Entity
@NotThreadSafe
public class ToponymBackupEntity extends AToponymEntity {

    private static final long serialVersionUID = 1L;

}
