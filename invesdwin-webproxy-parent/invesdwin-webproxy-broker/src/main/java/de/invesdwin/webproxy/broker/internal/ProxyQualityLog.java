package de.invesdwin.webproxy.broker.internal;

import java.io.File;
import java.io.IOException;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Named;

import de.invesdwin.context.ContextProperties;
import de.invesdwin.context.log.error.Err;
import de.invesdwin.util.lang.Files;
import de.invesdwin.util.time.date.FDate;
import de.invesdwin.webproxy.broker.contract.schema.ProxyQuality;

@Named
@ThreadSafe
public final class ProxyQualityLog {

    private static final File LOG_FILE = new File(ContextProperties.getCacheDirectory(),
            ProxyQualityLog.class.getSimpleName() + ".txt");
    private static final int MAX_COUNT_LOGS = 100;
    private static int countLogs = 0;

    private ProxyQualityLog() {
        Files.deleteQuietly(LOG_FILE);
    }

    public static synchronized boolean writeLog(final String clientIp, final String headers,
            final ProxyQuality result) {
        countLogs++;
        if (countLogs > MAX_COUNT_LOGS) {
            Files.deleteQuietly(LOG_FILE);
            countLogs = 1;
        }
        final StringBuilder sb = new StringBuilder();
        sb.append(new FDate());
        sb.append(": clientIp = [");
        sb.append(clientIp);
        sb.append("] resulted in: ");
        sb.append(result);
        sb.append("\n");
        sb.append(headers);
        sb.append("\n-------------------------------------------------------------------------\n\n\n\n");
        write(sb.toString());
        return true;
    }

    private static void write(final String entry) {
        try {
            Files.write(LOG_FILE, entry, true);
        } catch (final IOException e) {
            throw Err.process(e);
        }
    }

}
