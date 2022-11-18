package de.invesdwin.webproxy.geolocation.internal.geonames.persistence;

import javax.annotation.concurrent.NotThreadSafe;

import org.hibernate.validator.constraints.Length;

import de.invesdwin.context.persistence.jpa.api.dao.entity.identity.AEntityWithIdentity;
import de.invesdwin.context.persistence.jpa.api.index.Index;
import de.invesdwin.context.persistence.jpa.api.index.Indexes;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

@NotThreadSafe
@MappedSuperclass
@Indexes(@Index(columnNames = { "classification" }))
public abstract class AToponymEntity extends AEntityWithIdentity {

    @Column(nullable = false)
    private Integer classification;
    @Column(nullable = false)
    private Float latitude;
    @Column(nullable = false)
    private Float longitude;
    @Column(nullable = false)
    private String locationName;
    @Length(min = 2, max = 2)
    @Column(nullable = false)
    private String countryCode;
    @Length(min = 2)
    @Column(nullable = false)
    private String timeZoneId;

    public Integer getClassification() {
        return classification;
    }

    public void setClassification(final Integer classification) {
        this.classification = classification;
    }

    /**
     * from -90 to +90
     */
    public Float getLatitude() {
        return latitude;
    }

    public void setLatitude(final Float longitude) {
        this.latitude = longitude;
    }

    /**
     * from -180 to +180
     */
    public Float getLongitude() {
        return longitude;
    }

    public void setLongitude(final Float longitude) {
        this.longitude = longitude;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(final String locationName) {
        this.locationName = locationName;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(final String countryCode) {
        this.countryCode = countryCode;
    }

    public String getTimeZoneId() {
        return timeZoneId;
    }

    public void setTimeZoneId(final String timeZoneId) {
        this.timeZoneId = timeZoneId;
    }

}
