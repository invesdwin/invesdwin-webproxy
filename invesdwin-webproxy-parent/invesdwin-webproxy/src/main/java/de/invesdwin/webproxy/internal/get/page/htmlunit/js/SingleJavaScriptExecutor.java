package de.invesdwin.webproxy.internal.get.page.htmlunit.js;

import javax.annotation.concurrent.ThreadSafe;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.javascript.background.GAEJavaScriptExecutor;
import com.gargoylesoftware.htmlunit.javascript.background.JavaScriptJob;
import com.gargoylesoftware.htmlunit.javascript.background.JavaScriptJobManager;

import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.concurrent.Threads;

@ThreadSafe
public class SingleJavaScriptExecutor extends GAEJavaScriptExecutor {

    private static final long serialVersionUID = 1L;

    private volatile boolean registered;

    public SingleJavaScriptExecutor(final WebClient webClient) {
        super(webClient);
    }

    @Override
    public int pumpEventLoop(final long timeoutMillis) {
        final JavaScriptJobManager jobManager = getJobManagerWithEarliestJob();
        if (jobManager == null) {
            return 0;
        }

        int runCount = 0;
        while (jobManager.getJobCount() > 0 && !Threads.isInterrupted()) {
            final JavaScriptJob earliestJob = jobManager.getEarliestJob();
            if (earliestJob == null) {
                break;
            }

            final boolean ran = jobManager.runSingleJob(earliestJob);
            if (ran) {
                runCount++;
            } else {
                break;
            }
        }
        return runCount;
    }

    @Override
    protected void startThreadIfNeeded() {
        if (!registered) {
            SingleJavaScriptExecutorHandler.register(this);
            registered = true;
        }
    }

    @Override
    public void shutdown() {
        super.shutdown();
        Assertions.assertThat(registered).isTrue();
        SingleJavaScriptExecutorHandler.unregister(this);
        registered = false;
    }

}
