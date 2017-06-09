package de.invesdwin.webproxy.broker.internal;

import javax.annotation.concurrent.Immutable;

import de.invesdwin.context.system.properties.SystemProperties;
import de.invesdwin.util.time.duration.Duration;
import de.invesdwin.util.time.fdate.FDate;
import de.invesdwin.util.time.fdate.FTimeUnit;

@Immutable
public final class BrokerProperties {

    public static final Duration PROXY_DOWNTIME_TOLERANCE;
    public static final int MAX_SPECIFIC_TO_BE_SCANNED_PORTS;
    private static final int ADDITIONAL_RANDOM_TO_BE_SCANNED_PORTS_PERCENT;

    private static final SystemProperties SYSTEM_PROPERTIES = new SystemProperties(BrokerProperties.class);

    private BrokerProperties() {}

    static {
        PROXY_DOWNTIME_TOLERANCE = SYSTEM_PROPERTIES.getDuration("PROXY_DOWNTIME_TOLERANCE");
        MAX_SPECIFIC_TO_BE_SCANNED_PORTS = SYSTEM_PROPERTIES.getInteger("MAX_SPECIFIC_TO_BE_SCANNED_PORTS");
        ADDITIONAL_RANDOM_TO_BE_SCANNED_PORTS_PERCENT = SYSTEM_PROPERTIES
                .getInteger("ADDITIONAL_RANDOM_TO_BE_SCANNED_PORTS_PERCENT");
    }

    public static int calculateAdditionalRandomToBeScannedPorts(final int countSpecificPorts) {
        return (int) (((double) countSpecificPorts / 100)
                * BrokerProperties.ADDITIONAL_RANDOM_TO_BE_SCANNED_PORTS_PERCENT);
    }

    public static FDate calculateDowntimeToleranceExceededDate() {
        final FDate expired = new FDate()
                .addMilliseconds(-BrokerProperties.PROXY_DOWNTIME_TOLERANCE.intValue(FTimeUnit.MILLISECONDS));
        return expired;
    }

}
