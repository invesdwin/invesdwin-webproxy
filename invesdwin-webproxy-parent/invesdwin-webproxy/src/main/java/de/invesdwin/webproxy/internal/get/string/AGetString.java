package de.invesdwin.webproxy.internal.get.string;

import java.net.URI;

import javax.annotation.concurrent.ThreadSafe;

import de.invesdwin.webproxy.GetStringConfig;
import de.invesdwin.webproxy.broker.contract.schema.Proxy;
import de.invesdwin.webproxy.internal.get.ADownloadWorker;
import de.invesdwin.webproxy.internal.get.AGet;
import de.invesdwin.webproxy.internal.proxypool.IProxyPool;

@ThreadSafe
public abstract class AGetString extends AGet<String, GetStringConfig> {

    public AGetString(final IProxyPool pool, final boolean retryAllowed, final boolean proxyVerification) {
        super(pool, retryAllowed, proxyVerification);
    }

    @Override
    protected ADownloadWorker<String, GetStringConfig> newWorker(final GetStringConfig config, final URI uri,
            final Proxy proxy) {
        return new HttpGetWorker(config, uri, proxy);
    }

    @Override
    protected String responseToString(final String response) {
        return response;
    }

}
