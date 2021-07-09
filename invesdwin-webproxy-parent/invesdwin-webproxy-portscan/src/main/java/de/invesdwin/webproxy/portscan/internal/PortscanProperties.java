package de.invesdwin.webproxy.portscan.internal;

import java.net.InetAddress;

import javax.annotation.concurrent.Immutable;

import de.invesdwin.context.system.properties.SystemProperties;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.lang.uri.Addresses;
import de.invesdwin.util.time.date.FTimeUnit;
import de.invesdwin.util.time.duration.Duration;

/**
 * @see <a href="http://www.networkuptime.com/nmap/page09-09.shtml">Timings</a>
 * 
 * @author subes
 * 
 */
@Immutable
public final class PortscanProperties {

    public static final InetAddress CHECK_HOST;
    public static final int CHECK_PORT = 80;
    public static final int LOCAL_BIND_PORT;
    public static final Duration ICMP_RESPONSE_TIMEOUT;

    /**
     * To limit the own bandwidth.
     */
    public static final Duration UPLOAD_PAUSE_BETWEEN_PACKETS;
    /**
     * So that hosts are not stressed too much.
     */
    public static final Duration UPLOAD_PAUSE_BETWEEN_PACKETS_PER_HOST;
    /**
     * Automatic rate detection for hosts through maximum waiting time.
     */
    public static final Duration RESPONSE_TIMEOUT_BETWEEN_SYN_PACKETS_PER_HOST;
    public static final int MAX_PACKETS_PER_SECOND;
    public static final int MAX_OPEN_SYN_REQUESTS;
    public static final int MAX_OPEN_ICMP_REQUESTS;

    private static final SystemProperties SYSTEM_PROPERTIES = new SystemProperties(PortscanProperties.class);

    static {
        CHECK_HOST = Addresses.asAddress(SYSTEM_PROPERTIES.getString("CHECK_HOST"));
        LOCAL_BIND_PORT = readLocalBindPort();
        ICMP_RESPONSE_TIMEOUT = SYSTEM_PROPERTIES.getDuration("ICMP_RESPONSE_TIMEOUT");
        UPLOAD_PAUSE_BETWEEN_PACKETS = SYSTEM_PROPERTIES.getDuration("UPLOAD_PAUSE_BETWEEN_PACKETS");
        UPLOAD_PAUSE_BETWEEN_PACKETS_PER_HOST = SYSTEM_PROPERTIES.getDuration("UPLOAD_PAUSE_BETWEEN_PACKETS_PER_HOST");
        RESPONSE_TIMEOUT_BETWEEN_SYN_PACKETS_PER_HOST = SYSTEM_PROPERTIES
                .getDuration("RESPONSE_TIMEOUT_BETWEEN_SYN_PACKETS_PER_HOST");
        MAX_PACKETS_PER_SECOND = calculateMaxPacketsPerSecond();
        MAX_OPEN_SYN_REQUESTS = SYSTEM_PROPERTIES.getInteger("MAX_OPEN_SYN_REQUESTS");
        MAX_OPEN_ICMP_REQUESTS = SYSTEM_PROPERTIES.getInteger("MAX_OPEN_ICMP_REQUESTS");
    }

    private PortscanProperties() {}

    private static int readLocalBindPort() {
        final String key = "LOCAL_BIND_PORT";
        final Integer value = SYSTEM_PROPERTIES.getInteger(key);
        Assertions.assertThat(Addresses.isPort(value))
                .as(SYSTEM_PROPERTIES.getErrorMessage(key, value, null,
                        "Value must be inclusively between " + Addresses.PORT_MIN + " and " + Addresses.PORT_MAX + "."))
                .isTrue();
        return value;
    }

    private static int calculateMaxPacketsPerSecond() {
        final int maxLimit = 500;
        final long pause = PortscanProperties.UPLOAD_PAUSE_BETWEEN_PACKETS.intValue(FTimeUnit.NANOSECONDS);
        if (pause == 0) {
            return maxLimit;
        } else {
            final long sekunde = new Duration(1, FTimeUnit.SECONDS).intValue(FTimeUnit.NANOSECONDS);
            final long maxPaketeProSekunde = sekunde / pause;
            return Math.min((int) maxPaketeProSekunde, maxLimit);
        }
    }
}
