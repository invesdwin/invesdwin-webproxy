package de.invesdwin.webproxy.internal.get;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.annotation.concurrent.ThreadSafe;

import de.invesdwin.context.log.Log;
import de.invesdwin.util.concurrent.Threads;
import de.invesdwin.util.time.Instant;
import de.invesdwin.util.time.duration.Duration;
import de.invesdwin.util.time.fdate.FTimeUnit;
import de.invesdwin.webproxy.broker.contract.schema.Proxy;

@ThreadSafe
public abstract class ADownloadWorker<E, C extends AGetConfig> implements Callable<E> {

    private final Log log = new Log(this);
    private final C config;
    private final URI uri;
    private final Proxy proxy;
    private volatile boolean closed;

    protected ADownloadWorker(final C config, final URI uri, final Proxy proxy) {
        if ((config.isUseProxyPool() || config.getFixedProxy() != null) && proxy == null) {
            throw new IllegalStateException("If a proxy should be used here, it should not be null!");
        }
        this.config = config;
        this.uri = uri;
        this.proxy = proxy;
    }

    @Override
    public final E call() throws IOException {
        try {
            return download(config, uri, proxy);
        } finally {
            closed = true;
        }
    }

    protected abstract E download(C config, URI uri, Proxy proxy) throws IOException;

    protected void close() throws InterruptedException {
        internalClose();
        final Instant waitBegin = new Instant();
        while (!closed) {
            Threads.throwIfInterrupted();
            if (new Duration(waitBegin).isGreaterThan(2, FTimeUnit.MINUTES)) {
                if (log.isWarnEnabled()) {
                    String warning = "The download cannot be closed for [" + uri + "] !";
                    if (proxy != null) {
                        warning += " Used proxy was: " + proxy.toString();
                    }
                    log.warn(warning);
                }
                return;
            }
            TimeUnit.SECONDS.sleep(1);
        }
    }

    protected abstract void internalClose();

}