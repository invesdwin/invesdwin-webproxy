package de.invesdwin.webproxy;

import javax.annotation.concurrent.Immutable;

import com.gargoylesoftware.htmlunit.BrowserVersion;

import de.invesdwin.context.system.properties.SystemProperties;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.time.duration.Duration;
import de.invesdwin.util.time.fdate.FTimeUnit;

@Immutable
public final class WebproxyProperties {

    public static final BrowserVersion DEFAULT_BROWSER_VERION = BrowserVersion.FIREFOX_24;
    public static final int MAX_PARALLEL_DOWNLOADS;

    public static final boolean PROXY_VERIFICATION_RETRY_ON_ALL_EXCEPTIONS;
    public static final Duration PROXY_VERIFICATION_REDIRECT_SLEEP;
    public static final Duration PROXY_POOL_WARMUP_TIMEOUT;
    public static final boolean PROXY_POOL_COOLDOWN_ALLOWED;
    public static final Duration PROXY_POOL_COOLDOWN_MIN_TIMEOUT;
    public static final Duration PROXY_POOL_COOLDOWN_MAX_TIMEOUT;
    public static final Duration DEFAULT_MAX_DOWNLOAD_TRY_DURATION;
    public static final int MAX_ABSOLUTE_DOWNLOAD_RETRIES;
    public static final int DEFAULT_MAX_DOWNLOAD_RETRIES;
    public static final boolean DEFAULT_MAX_DOWNLOAD_RETRIES_WARNING_ONLY;
    public static final boolean AUTO_NOTIFY_ABOUT_NOT_WORKING_POOLED_PROXIES;

    private static final SystemProperties SYSTEM_PROPERTIES = new SystemProperties(WebproxyProperties.class);

    static {
        MAX_PARALLEL_DOWNLOADS = SYSTEM_PROPERTIES.getInteger("MAX_PARALLEL_DOWNLOADS");
        PROXY_VERIFICATION_RETRY_ON_ALL_EXCEPTIONS = SYSTEM_PROPERTIES
                .getBoolean("PROXY_VERIFICATION_RETRY_ON_ALL_EXCEPTIONS");
        PROXY_VERIFICATION_REDIRECT_SLEEP = SYSTEM_PROPERTIES.getDuration("PROXY_VERIFICATION_REDIRECT_SLEEP");
        PROXY_POOL_WARMUP_TIMEOUT = SYSTEM_PROPERTIES.getDuration("PROXY_POOL_WARMUP_TIMEOUT");
        PROXY_POOL_COOLDOWN_ALLOWED = SYSTEM_PROPERTIES.getBoolean("PROXY_POOL_COOLDOWN_ALLOWED");
        PROXY_POOL_COOLDOWN_MIN_TIMEOUT = SYSTEM_PROPERTIES.getDuration("PROXY_POOL_COOLDOWN_MIN_TIMEOUT");
        PROXY_POOL_COOLDOWN_MAX_TIMEOUT = SYSTEM_PROPERTIES.getDuration("PROXY_POOL_COOLDOWN_MAX_TIMEOUT");
        Assertions.assertThat(PROXY_POOL_COOLDOWN_MIN_TIMEOUT.longValue(FTimeUnit.NANOSECONDS))
                .as("PROXY_POOL_COOLDOWN_MIN_TIMEOUT muss kleiner gleich PROXY_POOL_COOLDOWN_MAX_TIMEOUT sein!")
                .isLessThanOrEqualTo(PROXY_POOL_COOLDOWN_MAX_TIMEOUT.longValue(FTimeUnit.NANOSECONDS));
        DEFAULT_MAX_DOWNLOAD_TRY_DURATION = SYSTEM_PROPERTIES.getDuration("DEFAULT_MAX_DOWNLOAD_TRY_DURATION");
        MAX_ABSOLUTE_DOWNLOAD_RETRIES = SYSTEM_PROPERTIES.getInteger("MAX_ABSOLUTE_DOWNLOAD_RETRIES");
        DEFAULT_MAX_DOWNLOAD_RETRIES = leseDefaultMaxRetries();
        DEFAULT_MAX_DOWNLOAD_RETRIES_WARNING_ONLY = SYSTEM_PROPERTIES
                .getBoolean("DEFAULT_MAX_DOWNLOAD_RETRIES_WARNING_ONLY");
        AUTO_NOTIFY_ABOUT_NOT_WORKING_POOLED_PROXIES = SYSTEM_PROPERTIES
                .getBoolean("AUTO_NOTIFY_ABOUT_NOT_WORKING_POOLED_PROXIES");
    }

    private WebproxyProperties() {}

    private static int leseDefaultMaxRetries() {
        final String key = "DEFAULT_MAX_DOWNLOAD_RETRIES";
        final int max = SYSTEM_PROPERTIES.getInteger(key);
        if (max > MAX_ABSOLUTE_DOWNLOAD_RETRIES || max < 0) {
            throw new IllegalArgumentException(SYSTEM_PROPERTIES.getErrorMessage(key, max, null,
                    "Must be inclusively between 0 and " + (MAX_ABSOLUTE_DOWNLOAD_RETRIES - 1) + "."));
        }
        return max;
    }

}
