package de.invesdwin.webproxy.crawler;

import javax.annotation.concurrent.Immutable;

import de.invesdwin.context.system.properties.SystemProperties;
import de.invesdwin.util.time.date.FTimeUnit;
import de.invesdwin.util.time.duration.Duration;

@Immutable
public final class CrawlerProperties {

    public static final Duration MAX_RANDOM_SCAN_DURATION = new Duration(3, FTimeUnit.HOURS);
    public static final int CRAWL_WITH_PROXIES_THRESHOLD = 500;
    public static final boolean RANDOM_SCAN_ALLOWED;
    public static final boolean WAIT_FOR_PORTSCAN_PROCESSING_END;

    private static final SystemProperties SYSTEM_PROPERTIES = new SystemProperties(CrawlerProperties.class);

    private CrawlerProperties() {}

    static {
        RANDOM_SCAN_ALLOWED = SYSTEM_PROPERTIES.getBoolean("RANDOM_SCAN_ALLOWED");
        WAIT_FOR_PORTSCAN_PROCESSING_END = SYSTEM_PROPERTIES.getBoolean("WAIT_FOR_PORTSCAN_PROCESSING_END");
    }

}
