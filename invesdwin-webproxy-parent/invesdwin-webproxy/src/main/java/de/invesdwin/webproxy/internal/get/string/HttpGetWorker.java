package de.invesdwin.webproxy.internal.get.string;

import java.io.IOException;
import java.net.URI;

import javax.annotation.concurrent.ThreadSafe;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import de.invesdwin.webproxy.GetStringConfig;
import de.invesdwin.webproxy.broker.contract.schema.Proxy;
import de.invesdwin.webproxy.internal.get.ADownloadWorker;
import de.invesdwin.webproxy.internal.get.string.httpclient.HttpGetFactory;

@ThreadSafe
public class HttpGetWorker extends ADownloadWorker<String, GetStringConfig> {

    private final HttpGet httpGet;
    private final HttpContext httpContext;

    public HttpGetWorker(final GetStringConfig config, final URI uri, final Proxy proxy) {
        super(config, uri, proxy);
        httpGet = HttpGetFactory.newHttpGet(uri, proxy, config.getBrowserVersion());
        httpContext = HttpGetFactory.newHttpContext(proxy);
    }

    @Override
    protected String download(final GetStringConfig config, final URI uri, final Proxy proxy) throws IOException {
        final HttpResponse response = HttpGetFactory.SHARED_CLIENT.execute(httpGet, httpContext);
        final HttpEntity entity = response.getEntity();
        /*
         * entity cannot be null as it seems:
         * http://hc.apache.org/httpcomponents-client-4.0.1/tutorial/html/fundamentals.html
         */
        if (entity == null) {
            throw new IOException("Entity is null. This may be a sign that the download should be retried.");
        }
        final String content = EntityUtils.toString(entity);
        EntityUtils.consume(entity);
        return content;
    }

    @Override
    protected void internalClose() {
        httpGet.abort();
    }

}