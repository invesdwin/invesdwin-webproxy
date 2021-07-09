package de.invesdwin.webproxy.broker.internal.persistence;

import java.net.InetSocketAddress;
import java.util.Date;

import javax.annotation.concurrent.NotThreadSafe;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import de.invesdwin.context.persistence.jpa.api.dao.entity.identity.AEntityWithIdentity;
import de.invesdwin.util.lang.uri.Addresses;
import de.invesdwin.util.time.date.FDate;
import de.invesdwin.util.time.date.FDates;
import de.invesdwin.webproxy.broker.contract.schema.Proxy;
import de.invesdwin.webproxy.broker.contract.schema.ProxyQuality;
import de.invesdwin.webproxy.broker.contract.schema.ProxyType;
import de.invesdwin.webproxy.broker.contract.schema.RawProxy;

@NotThreadSafe
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "host", "port" }) })
public class ProxyEntity extends AEntityWithIdentity {

    private static final long serialVersionUID = 1L;

    private String host;
    @Min(Addresses.PORT_MIN)
    @Max(Addresses.PORT_MAX)
    private Integer port;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ProxyType type;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ProxyQuality quality;
    @Column(nullable = false)
    private String countryCode;
    @Column(nullable = false)
    private String timeZoneId;
    @Column(nullable = false)
    private Date lastSuccessful;

    public String getHost() {
        return host;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(final Integer port) {
        this.port = port;
    }

    public ProxyType getType() {
        return type;
    }

    public void setType(final ProxyType type) {
        this.type = type;
    }

    public ProxyQuality getQuality() {
        return quality;
    }

    public void setQuality(final ProxyQuality quality) {
        this.quality = quality;
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

    public FDate getLastSuccessful() {
        return FDate.valueOf(lastSuccessful);
    }

    public void setLastSuccessful(final FDate lastSuccessful) {
        this.lastSuccessful = FDates.toDate(lastSuccessful);
    }

    public static ProxyEntity valueOf(final Proxy proxy) {
        final ProxyEntity ent = new ProxyEntity();
        ent.mergeFrom(proxy);
        return ent;
    }

    public static ProxyEntity valueOf(final RawProxy proxy) {
        final ProxyEntity ent = new ProxyEntity();
        ent.mergeFrom(proxy);
        return ent;
    }

    public Proxy toProxy() {
        final Proxy proxy = new Proxy();
        proxy.mergeFrom(this);
        return proxy;
    }

    public static ProxyEntity valueOf(final String host, final Integer port, final ProxyType type,
            final ProxyQuality quality, final String countryCode, final String timeZoneId) {
        final ProxyEntity ent = new ProxyEntity();
        ent.setHost(host);
        ent.setPort(port);
        ent.setType(type);
        ent.setQuality(quality);
        ent.setCountryCode(countryCode);
        ent.setTimeZoneId(timeZoneId);
        return ent;
    }

    public static ProxyEntity valueOf(final java.net.Proxy proxy) {
        final ProxyEntity ent = new ProxyEntity();
        final InetSocketAddress addr = (InetSocketAddress) proxy.address();
        ent.setHost(addr.getHostName());
        ent.setPort(addr.getPort());
        ent.setType(ProxyType.valueOf(proxy.type().toString()));
        return ent;
    }

}
