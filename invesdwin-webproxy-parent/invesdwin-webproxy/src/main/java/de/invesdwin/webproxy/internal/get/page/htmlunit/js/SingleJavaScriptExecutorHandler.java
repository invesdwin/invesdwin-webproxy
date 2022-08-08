package de.invesdwin.webproxy.internal.get.page.htmlunit.js;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import de.invesdwin.context.log.error.Err;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.collections.Collections;
import de.invesdwin.util.concurrent.Executors;
import de.invesdwin.util.concurrent.Threads;
import de.invesdwin.util.concurrent.WrappedExecutorService;

@ThreadSafe
public final class SingleJavaScriptExecutorHandler {

    private static final Set<SingleJavaScriptExecutor> JOBS = Collections
            .newSetFromMap(new ConcurrentHashMap<SingleJavaScriptExecutor, Boolean>());

    @GuardedBy("SingleJavaScriptExecutorHandler.class")
    private static WrappedExecutorService executor;

    private SingleJavaScriptExecutorHandler() {
    }

    public static synchronized void register(final SingleJavaScriptExecutor job) {
        try {
            Assertions.assertThat(JOBS.add(job)).isTrue();
            if (executor == null) {
                executor = Executors.newFixedThreadPool(SingleJavaScriptExecutorHandler.class.getSimpleName(), 1);
                executor.execute(new JobRunner());
            }
        } catch (final Throwable t) {
            throw Err.process(t);
        }
    }

    public static synchronized void unregister(final SingleJavaScriptExecutor job) {
        try {
            Assertions.assertThat(JOBS.remove(job)).isTrue();
            if (JOBS.size() == 0) {
                try {
                    executor.shutdownNow();
                    executor.awaitTermination();
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    executor = null;
                }
            }
        } catch (final Throwable t) {
            throw Err.process(t);
        }
    }

    private static class JobRunner implements Runnable {
        @Override
        public void run() {
            while (!Threads.isInterrupted()) {
                int runCount = 0;
                for (final SingleJavaScriptExecutor job : JOBS) {
                    runCount += job.pumpEventLoop();
                    if (Threads.isInterrupted()) {
                        return;
                    }
                }
                if (runCount == 0) {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (final InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        }

    }

}
