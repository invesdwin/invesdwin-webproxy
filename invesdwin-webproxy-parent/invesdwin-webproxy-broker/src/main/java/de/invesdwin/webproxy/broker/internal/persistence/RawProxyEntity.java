package de.invesdwin.webproxy.broker.internal.persistence;

import javax.annotation.concurrent.NotThreadSafe;

import de.invesdwin.context.persistence.jpa.api.dao.entity.identity.AEntityWithIdentity;
import de.invesdwin.util.lang.uri.Addresses;
import de.invesdwin.webproxy.broker.contract.schema.RawProxy;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@NotThreadSafe
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "host", "port" }) })
public class RawProxyEntity extends AEntityWithIdentity {

    private static final long serialVersionUID = 1L;

    private String host;
    @Min(Addresses.PORT_MIN)
    @Max(Addresses.PORT_MAX)
    private Integer port;

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

    public static RawProxyEntity valueOf(final ProxyEntity proxy) {
        final RawProxyEntity ent = new RawProxyEntity();
        ent.setHost(proxy.getHost());
        ent.setPort(proxy.getPort());
        return ent;
    }

    public static RawProxyEntity valueOf(final RawProxy proxy) {
        final RawProxyEntity ent = new RawProxyEntity();
        ent.setHost(proxy.getHost());
        ent.setPort(proxy.getPort());
        return ent;
    }

    public static RawProxyEntity valueOf(final String host, final Integer port) {
        final RawProxyEntity ent = new RawProxyEntity();
        ent.setHost(host);
        ent.setPort(port);
        return ent;
    }

    public RawProxy toRawProxy() {
        final RawProxy rawProxy = new RawProxy();
        rawProxy.setHost(getHost());
        rawProxy.setPort(getPort());
        return rawProxy;
    }

}
