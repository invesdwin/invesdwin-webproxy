package de.invesdwin.webproxy.internal.get.page.htmlunit.js;

import javax.annotation.concurrent.ThreadSafe;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.javascript.background.BackgroundJavaScriptFactory;
import com.gargoylesoftware.htmlunit.javascript.background.JavaScriptExecutor;

@ThreadSafe
public class HackedBackgroundJavaScriptFactory extends BackgroundJavaScriptFactory {

    @Override
    public JavaScriptExecutor createJavaScriptExecutor(final WebClient webClient) {
        if (webClient.getOptions().isJavaScriptEnabled()) {
            return new SingleJavaScriptExecutor(webClient);
        } else {
            // Even with JS deactivated, without this workaround the executor still runs!
            return new DisabledJavaScriptExecutor(webClient);
        }
    }

}
