package de.invesdwin.webproxy.internal;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import javax.inject.Named;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;

import de.invesdwin.webproxy.GetPageConfig;
import de.invesdwin.webproxy.GetStringConfig;
import de.invesdwin.webproxy.IWebproxyService;
import de.invesdwin.webproxy.broker.contract.schema.Proxy;
import de.invesdwin.webproxy.internal.get.page.AGetPage;
import de.invesdwin.webproxy.internal.get.page.DirectGetPage;
import de.invesdwin.webproxy.internal.get.page.FixedProxyGetPage;
import de.invesdwin.webproxy.internal.get.page.ProxyPoolGetPage;
import de.invesdwin.webproxy.internal.get.string.AGetString;
import de.invesdwin.webproxy.internal.get.string.DirectGetString;
import de.invesdwin.webproxy.internal.get.string.FixedProxyGetString;
import de.invesdwin.webproxy.internal.get.string.ProxyPoolGetString;

/**
 * 
 * Generally URIs are being used instead of URLs, because any URL is also an URI and because URLs are inperformant in
 * Sets in Java.
 * 
 * @see <a href="http://findbugs.sourceforge.net/bugDescriptions.html#DMI_COLLECTION_OF_URLS">Why URLs are evil</a>
 * 
 * @author subes
 * 
 */
@Named
@ThreadSafe
public class WebproxyService implements IWebproxyService {

    @Inject
    private DirectGetString directGetString;
    @Inject
    private ProxyPoolGetString proxyPoolGetString;

    @Inject
    private DirectGetPage directGetPage;
    @Inject
    private ProxyPoolGetPage proxyPoolGetPage;

    @Inject
    private WebproxyServiceHelper helper;

    @Override
    public Proxy newProxy(final GetStringConfig config) throws InterruptedException {
        return helper.newProxy(config);
    }

    @Override
    public WebClient newWebClient(final GetPageConfig config) {
        return helper.newWebClient(config);
    }

    /************************** getString ********************************/

    @Override
    public Future<String> getString(final GetStringConfig config, final URI uri) throws InterruptedException {
        return aGetString(config).get(config, uri);
    }

    @Override
    public List<Future<String>> getString(final GetStringConfig config, final Collection<URI> uris)
            throws InterruptedException {
        return aGetString(config).get(config, uris);
    }

    /************************* getPage *************************/

    @Override
    public <T extends Page> Future<T> getPage(final GetPageConfig config, final URI uri) throws InterruptedException {
        return aGetPage(config).get(config, uri);
    }

    @Override
    public <T extends Page> List<Future<T>> getPage(final GetPageConfig config, final Collection<URI> uris)
            throws InterruptedException {
        return aGetPage(config).get(config, uris);
    }

    /*************************** private ********************/

    private AGetString aGetString(final GetStringConfig config) {
        if (config.getFixedProxy() != null) {
            return new FixedProxyGetString(config.getFixedProxy(), false);
        } else if (config.isUseProxyPool()) {
            return proxyPoolGetString;
        } else {
            return directGetString;
        }
    }

    private AGetPage aGetPage(final GetPageConfig config) {
        if (config.getFixedProxy() != null) {
            return new FixedProxyGetPage(config.getFixedProxy());
        } else if (config.isUseProxyPool()) {
            return proxyPoolGetPage;
        } else {
            return directGetPage;
        }
    }

    @Override
    public int getWorkingProxiesCount() {
        return helper.getLastWorkingProxyCount();
    }

}
