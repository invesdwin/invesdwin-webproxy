package de.invesdwin.webproxy.crawler.sources;

import java.net.URI;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Named;

import de.invesdwin.util.lang.uri.URIs;

@ThreadSafe
@Named
public class MultiProxyOrgCrawlerSource extends AUriProxyCrawlerSourceTemplate {

    @Override
    protected URI getUri() {
        return URIs.asUri("http://www.multiproxy.org/txt_all/proxy.txt");
    }

}
