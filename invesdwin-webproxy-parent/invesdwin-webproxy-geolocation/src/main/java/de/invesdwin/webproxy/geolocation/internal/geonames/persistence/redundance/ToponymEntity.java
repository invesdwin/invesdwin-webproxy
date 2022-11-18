package de.invesdwin.webproxy.geolocation.internal.geonames.persistence.redundance;

import javax.annotation.concurrent.NotThreadSafe;

import de.invesdwin.webproxy.geolocation.internal.geonames.persistence.AToponymEntity;
import jakarta.persistence.Entity;

@Entity
@NotThreadSafe
public class ToponymEntity extends AToponymEntity {

    private static final long serialVersionUID = 1L;

}
