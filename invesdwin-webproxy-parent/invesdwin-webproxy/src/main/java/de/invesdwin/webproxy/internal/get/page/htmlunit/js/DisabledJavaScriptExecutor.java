package de.invesdwin.webproxy.internal.get.page.htmlunit.js;

import javax.annotation.concurrent.ThreadSafe;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.javascript.background.DefaultJavaScriptExecutor;

/**
 * A dummy executor that does not start any thread.
 */
@SuppressWarnings("serial")
@ThreadSafe
public class DisabledJavaScriptExecutor extends DefaultJavaScriptExecutor {

    public DisabledJavaScriptExecutor(final WebClient webClient) {
        super(webClient);
    }

    @Override
    protected void startThreadIfNeeded() {};

}
