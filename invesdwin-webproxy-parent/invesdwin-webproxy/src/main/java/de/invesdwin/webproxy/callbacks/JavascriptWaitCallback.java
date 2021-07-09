package de.invesdwin.webproxy.callbacks;

import java.io.Serializable;

import javax.annotation.concurrent.Immutable;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;

import de.invesdwin.util.time.date.FTimeUnit;
import de.invesdwin.util.time.duration.Duration;

/**
 * With this class you can delay the closing of the virtual browser window and thus delay the stopping of the javascript
 * threads. You can use this class to wait for JavaScript/Ajax progress that occurs after the page has been downloaded.
 * 
 * As a rule of thumb you should delay 1/10 of the maxDelay. E.g. with a maxDelay of 10 seconds, you delay 10 times for
 * 1 second. This makes it less resource intensive.
 * 
 * @author subes
 * 
 */
@Immutable
public class JavascriptWaitCallback implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Returns an upper boundary for the delay. When this is reached, the delay method won't be called anymore and thus
     * the waiting will get aborted.
     */
    public Duration getMaxDelay() {
        return new Duration(5, FTimeUnit.MINUTES);
    }

    /**
     * Returns true if the condition is met, for which the delay is being done.
     */
    public boolean isDownloadFinished(final WebClient client, final Page page) {
        return !client.getJavaScriptEngine().isScriptRunning();
    }

    /**
     * The delay itself. This method gets called regularly until the maxDelay is reached.
     * 
     * Most interesting are the methods client.waitForBackgroundJavaScript(millis) and
     * client.waitForBackgroundJavaScriptStartingBefore(millis) to create the actual delay.
     */
    public void delay(final WebClient client) {
        client.waitForBackgroundJavaScript(new Duration(10, FTimeUnit.SECONDS).longValue(FTimeUnit.MILLISECONDS));
    }

}
