package de.invesdwin.webproxy.internal.get;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

import de.invesdwin.context.integration.network.NetworkUtil;
import de.invesdwin.context.log.Log;
import de.invesdwin.context.log.error.Err;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.collections.Arrays;
import de.invesdwin.util.concurrent.Executors;
import de.invesdwin.util.concurrent.Threads;
import de.invesdwin.util.concurrent.WrappedExecutorService;
import de.invesdwin.util.error.Throwables;
import de.invesdwin.util.time.duration.Duration;
import de.invesdwin.webproxy.IllegalProxyResponseException;
import de.invesdwin.webproxy.ProxyVerification;
import de.invesdwin.webproxy.WebproxyProperties;
import de.invesdwin.webproxy.broker.contract.schema.Proxy;
import de.invesdwin.webproxy.callbacks.AProxyResponseCallback;
import de.invesdwin.webproxy.callbacks.statistics.basis.AStatisticsCallback;
import de.invesdwin.webproxy.callbacks.statistics.basis.SessionStatistics;
import de.invesdwin.webproxy.internal.proxypool.IProxyPool;
import de.invesdwin.webproxy.internal.proxypool.PooledProxy;

@ThreadSafe
public abstract class AGet<E, C extends AGetConfig> {

    /**
     * This Executor is used to ensure the maximum number of parallel downloads.
     */
    private static final WrappedExecutorService GET_EXECUTOR = Executors
            .newFixedThreadPool(AGet.class.getSimpleName() + "_Get", WebproxyProperties.MAX_PARALLEL_DOWNLOADS)
            .setWaitOnFullPendingCount(false);
    /**
     * This Executor is used to ensure the maximum number of proxy verifications regardless of normal downloads.
     */
    private static final WrappedExecutorService VERIFICATION_EXECUTOR = Executors
            .newFixedThreadPool(AGet.class.getSimpleName() + "_Verification", WebproxyProperties.MAX_PARALLEL_DOWNLOADS)
            .setWaitOnFullPendingCount(true);
    /**
     * This Executor does not have any limit. Though it is used inside the other executors, so that the downloads
     * themself can run seperately and be monitored by the outer thread to maybe cancel the download.
     */
    private static final WrappedExecutorService CANCELLABLE_INNERER_EXECUTOR = Executors
            .newFixedThreadPool(AGet.class.getSimpleName() + "_Cancellable",
                    GET_EXECUTOR.getMaximumPoolSize() + VERIFICATION_EXECUTOR.getMaximumPoolSize())
            .setWaitOnFullPendingCount(true);

    @Inject
    private ProxyVerification proxyVeri;

    private final IProxyPool pool;
    private final boolean retryAllowed;
    private final boolean proxyVerification;

    public AGet(final IProxyPool pool, final boolean retryAllowed, final boolean proxyVerification) {
        this.pool = pool;
        this.retryAllowed = retryAllowed;
        this.proxyVerification = proxyVerification;
    }

    @SuppressWarnings("unchecked")
    public <T extends E> Future<T> get(final C config, final URI uri) throws InterruptedException {
        return (Future<T>) get(config, Arrays.asList(uri)).get(0);
    }

    public <T extends E> List<Future<T>> get(final C config, final Collection<URI> uris) throws InterruptedException {
        config.validate(proxyVerification);
        final ExecutorService executor = getRelevantExecutor(config);
        final List<Future<T>> futures = new ArrayList<Future<T>>();

        final Collection<URI> filteredUris = config.filterVisitedUri(uris);
        final SessionStatistics session = SessionStatistics.newSessionStatistics(config.getStatisticsCallback());
        final AtomicInteger endedDownloadsInSession = new AtomicInteger();
        final int countDownloadsInSession = filteredUris.size();

        if (config.getMaxParallelDownloads() != null
                && config.getMaxParallelDownloads() < WebproxyProperties.MAX_PARALLEL_DOWNLOADS) {
            //again limit parallel count
            final ExecutorService limitedExecutor = Executors
                    .newFixedThreadPool(AGet.class.getSimpleName() + "_Limited", config.getMaxParallelDownloads());
            for (final URI u : filteredUris) {
                final RetryingDownloadWorker<T> worker = new RetryingDownloadWorker<T>(config, u, session,
                        endedDownloadsInSession, countDownloadsInSession);
                final DelegateDownloadWorker<T> delegate = new DelegateDownloadWorker<T>(executor, worker);
                futures.add(limitedExecutor.submit(delegate));
            }
            limitedExecutor.shutdown();
        } else {
            //execute directly
            for (final URI u : filteredUris) {
                final RetryingDownloadWorker<T> worker = new RetryingDownloadWorker<T>(config, u, session,
                        endedDownloadsInSession, countDownloadsInSession);
                futures.add(executor.submit(worker));
            }
        }
        return futures;
    }

    /********************************* protected *************************************/

    /**
     * Verifications need to run separately or else waiting downloads cause deadlocks if they wait for proxies that
     * themself wait for free verification slots. Waiting for pendingCount on every Executor does not cause any
     * deadlocks, because they don't delegate tasks between each other. The only exception are GET-Threads that start
     * Verification-Threads.
     */
    protected ExecutorService getRelevantExecutor(final C config) throws InterruptedException {
        if (proxyVerification) {
            return VERIFICATION_EXECUTOR;
        } else {
            return GET_EXECUTOR;
        }
    }

    protected abstract <T extends E> ADownloadWorker<T, C> newWorker(C config, URI uri, Proxy proxy);

    protected abstract String responseToString(E response);

    /******************************** private ***********************************/

    private class DelegateDownloadWorker<T extends E> implements Callable<T> {

        private final RetryingDownloadWorker<T> worker;
        private final ExecutorService executor;

        DelegateDownloadWorker(final ExecutorService executor, final RetryingDownloadWorker<T> worker) {
            this.executor = executor;
            this.worker = worker;
        }

        @Override
        public T call() throws Exception {
            return executor.submit(worker).get();
        }

    }

    private class RetryingDownloadWorker<T extends E> implements Callable<T> {

        private final Log log = new Log(this);

        private final SessionStatistics session;
        private final C config;
        private final URI uri;
        private final TempDownloadInformation infos;

        private int absoluteRetries;
        private PooledProxy proxy;

        RetryingDownloadWorker(final C config, final URI uri, final SessionStatistics session,
                final AtomicInteger endedDownloadsInSession, final int countDownloadsInSession) {
            Assertions.assertThat((session == null) == (config.getStatisticsCallback() == null)).isTrue();
            this.session = session;
            this.config = config;
            this.uri = uri;
            this.infos = new TempDownloadInformation(endedDownloadsInSession, countDownloadsInSession);
        }

        @Override
        public T call() throws InterruptedException, IOException {
            boolean successful = false;
            try {
                //If the URI has been filtered, we don't do anything with this element
                if (uri == null) {
                    return null;
                }

                //Choose the first proxy
                proxy = pool.getProxy(config, session);

                T response = null;
                do {
                    response = workerDownloadTry();
                    if (response == null) {
                        throw new IOException("Download returned null!");
                    }
                } while (!isValidResponse(response));

                //Successful download
                final AStatisticsCallback callback = config.getStatisticsCallback();
                if (callback != null) {
                    callback.downloadSuccessful(session, infos.toStatistics(proxy != null, uri));
                }
                successful = true;
                return response;
            } catch (final Throwable e) {
                //Failed download
                final AStatisticsCallback callback = config.getStatisticsCallback();
                if (callback != null) {
                    callback.downloadFailure(session, infos.toStatistics(proxy != null, uri), e);
                }
                throw new IOException("For URL: " + uri, e);
            } finally {
                stopDownload(successful);
            }
        }

        private void stopDownload(final boolean successful) {
            if (proxy != null) {
                pool.returnProxy(config, session, proxy, successful);
                proxy = null;
            }
            if (infos.downloadEnded()) {
                final AStatisticsCallback callback = config.getStatisticsCallback();
                if (callback != null && session.getSessionStart() != null) {
                    callback.downloadSessionEnded(session);
                }
            }
        }

        private boolean isValidResponse(final T response) throws InterruptedException, IOException {
            final String sResponse = responseToString(response);
            final boolean retryBecauseOfInvalidResultFromProxy = proxy != null
                    && !callbackIsValidResponse(config.getProxyResponseCallback(), sResponse, response);
            if (retryBecauseOfInvalidResultFromProxy) {
                final IllegalProxyResponseException e = new IllegalProxyResponseException(sResponse);
                if (isRetryOk(e, sResponse, response)) {
                    return false;
                } else {
                    throw newRetriesExceededException(e);
                }
            } else if (callbackIsAnotherDownloadNeededForVerificationNeeded(config.getProxyResponseCallback(),
                    sResponse, response)) {
                /*
                 * Switch proxy once. Because of proxy rotation we get a new one here. If no proxy pool is used or a
                 * fixed one is used, we still retry even if it might not help. The download of the proxy does not get
                 * regarded as a failure, so that the proxy does not get dismissed completely, which would only cost
                 * performance.
                 */
                pool.returnProxy(config, session, proxy, true);
                proxy = pool.getProxy(config, session);
                throwIfAbsoluteRetriesExceeded(new IOException("Verification of the response is needed."));
                return false;
            } else {
                return true;
            }
        }

        private T workerDownloadTry() throws IOException, InterruptedException {
            final ADownloadWorker<T, C> worker = newWorker(config, uri, proxy);
            try {
                final Future<T> future = CANCELLABLE_INNERER_EXECUTOR.submit(worker);
                final Duration timeout = config.getMaxDownloadTryDuration();
                try {
                    return future.get(timeout.longValue(), timeout.getTimeUnit().timeUnitValue());
                } catch (final ExecutionException e) {
                    final IOException ioCause = Throwables.getCauseByType(e, IOException.class);
                    if (ioCause != null) {
                        throw ioCause;
                    } else {
                        throw e;
                    }
                } catch (final TimeoutException e) {
                    if (log.isWarnEnabled() && !proxyVerification) {
                        String warning = "MaxConnectionDuration of " + timeout + " exceeded for [" + uri + "]!";
                        if (proxy != null) {
                            warning += " Used proxy was: " + proxy.toString();
                        }
                        log.warn(warning);
                    }
                    //connection gets closed by outer finally
                    future.cancel(true);
                    throw new IOException("MaxConnectionDuration exceeded", e);
                }
            } catch (final Throwable e) {
                if (isRetryOk(e, e.getMessage(), e)) {
                    return workerDownloadTry();
                } else {
                    throw newRetriesExceededException(e);
                }
            } finally {
                worker.close();
            }
        }

        private boolean isRetryOk(final Throwable retryReason, final String withResponse,
                final Object withOriginalResponse) throws InterruptedException, IOException {
            //also checks if interrupted
            if (NetworkUtil.waitIfInternetNotAvailable()) {
                //if internet is gone, retry when its back
                return true;
            }
            throwIfAbsoluteRetriesExceeded(retryReason);
            if (proxy != null) {
                //dismiss the old proxy in any case (it might have cached the verification response)
                pool.discardProxy(config, session, proxy, retryReason);
                proxy = null;
            }
            final boolean isRetryOk;
            //anyway choose a new proxy
            proxy = pool.getProxy(config, session);
            if (proxy == null || (!proxyVerification
                    && callbackIsValidResponse(IsNotProxiesFaultProxyResponseCallback.INSTANCE, withResponse,
                            withOriginalResponse)
                    && proxyVeri.verifyProxy(proxy, false, config.getMinProxyQuality()))) {
                isRetryOk = incrementDownloadTries(retryReason);
            } else {
                isRetryOk = retryAllowed;
            }
            //Check for interrupted again at the end, because ProxyResponseCallback might have used sleep
            Threads.throwIfInterrupted();
            return isRetryOk;
        }

        private boolean incrementDownloadTries(final Throwable retryReason) {
            //only if not caused by the proxy does the retry get checked and counted
            infos.incrementNeededRetries();
            final boolean isRetryOk = retryAllowed && (infos.getNeededRetries() <= config.getMaxDownloadRetries()
                    || config.getMaxDownloadRetriesWarningOnly());
            if (retryAllowed && infos.getNeededRetries() == config.getMaxDownloadRetries()
                    && config.getMaxDownloadRetriesWarningOnly()) {
                //only warn once and still retry if wanted
                Err.process(newRetriesExceededException(retryReason));
            }
            return isRetryOk;
        }

        private boolean callbackIsValidResponse(final AProxyResponseCallback callback, final String response,
                final Object originalResponse) {
            return callback == null || (response != null && callback.isValidResponse(uri, response, originalResponse));
        }

        private boolean callbackIsAnotherDownloadNeededForVerificationNeeded(final AProxyResponseCallback callback,
                final String response, final Object originalResponse) {
            return proxy != null && callback != null
                    && callback.isAnotherDownloadNeededForVerification(uri, response, originalResponse);
        }

        /**
         * To be sure that the rules for retries are actually used.
         */
        private void throwIfAbsoluteRetriesExceeded(final Throwable retryReason)
                throws IOException, InterruptedException {
            Threads.throwIfInterrupted();
            absoluteRetries++;
            if (absoluteRetries >= WebproxyProperties.MAX_ABSOLUTE_DOWNLOAD_RETRIES) {
                final StringBuilder message = new StringBuilder();
                message.append("MaxAbsoluteDownloadRetries of ");
                message.append(WebproxyProperties.MAX_ABSOLUTE_DOWNLOAD_RETRIES);
                message.append(" exceeded. ");
                if (!config.getMaxDownloadRetriesWarningOnly()) {
                    message.append("Normally the normal MaxDownloadRetries of ");
                    message.append(config.getMaxDownloadRetries());
                    message.append(" should have been exceeded much earlier. ");
                }
                message.append(retryReason.toString());
                throw new IOException(message.toString(), retryReason);
            }
        }

        private IOException newRetriesExceededException(final Throwable cause) {
            final StringBuilder message = new StringBuilder();
            if (retryAllowed) {
                message.append("MaxDownloadRetries of ");
                message.append(config.getMaxDownloadRetries());
                message.append(" exceeded.");
                if (config.getMaxDownloadRetriesWarningOnly()) {
                    message.append(" This is configured just as a warning, thus retries are still being continued.");
                }
            } else {
                message.append("No retries allowed.");
            }
            message.append(" At: ");
            message.append(uri);
            return new IOException(message.toString(), cause);
        }

    }

}
