package de.invesdwin.webproxy.internal.get.page.htmlunit;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.concurrent.ThreadSafe;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WaitingRefreshHandler;

import de.invesdwin.context.ContextProperties;
import de.invesdwin.util.time.date.FTimeUnit;
import de.invesdwin.webproxy.GetPageConfig;

/**
 * Refresh to http://de.finance.yahoo.com/lookup/stocks?s=EC&t=S&m=ALL&r=&b=640 (1s) aborted by HtmlUnit: Attempted to
 * refresh a page using an ImmediateRefreshHandler which could have caused an OutOfMemoryError Please use
 * WaitingRefreshHandler or ThreadedRefreshHandler instead.
 * 
 * This handler only refreshes a given number of times.
 */
@ThreadSafe
public class CountingRefreshHandler extends WaitingRefreshHandler {

    private final GetPageConfig config;
    private final AtomicInteger refreshCount = new AtomicInteger();

    public CountingRefreshHandler(final GetPageConfig config) {
        super(ContextProperties.DEFAULT_NETWORK_TIMEOUT.intValue(FTimeUnit.SECONDS));
        this.config = config;
    }

    @Override
    public void handleRefresh(final Page page, final URL url, final int requestedWait) throws IOException {
        if (refreshCount.incrementAndGet() > config.getMaxPageRefreshCount()) {
            return;
        } else {
            super.handleRefresh(page, url, requestedWait);
        }
    }

}
