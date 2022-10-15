package de.invesdwin.webproxy.crawler.sources;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.concurrent.ThreadSafe;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import de.invesdwin.util.error.UnknownArgumentException;
import de.invesdwin.util.lang.string.Strings;
import de.invesdwin.util.lang.uri.Addresses;
import de.invesdwin.webproxy.broker.contract.ProxyUtil;
import de.invesdwin.webproxy.broker.contract.schema.RawProxy;

@ThreadSafe
public abstract class AProxyCrawlerSource implements IProxyCrawlerSource {

    protected Set<RawProxy> extractRawProxies(final Page page) {
        final Set<RawProxy> proxies = new HashSet<RawProxy>();
        if (page != null) {
            String pageContent = pageToString(page);
            pageContent = Strings.replaceNewlines(pageContent, getNewlineReplacement());
            final String[] textFragments = pageContent.split("\\s");

            String curHost = null;
            for (int i = 0; i < textFragments.length; i++) {
                final String textFragment = Strings.substringBefore(textFragments[i], "@");
                if (Strings.isBlank(textFragment)) {
                    continue;
                } else if (Addresses.isIpWithPort(textFragment)) {
                    final String[] splitAddr = textFragment.split(":");
                    curHost = splitAddr[0];
                    final String port = splitAddr[1];
                    proxies.add(extractRawProxy(curHost, port));
                    curHost = null;
                } else if (Addresses.isIp(textFragment)) {
                    if (curHost != null) {
                        proxies.add(extractRawProxy(curHost, null));
                        curHost = null;
                    }
                    curHost = textFragment;
                } else if (curHost != null && Addresses.isPort(textFragment)) {
                    final String port = textFragment;
                    proxies.add(extractRawProxy(curHost, port));
                    curHost = null;
                }
            }
        }
        return proxies;
    }

    protected String getNewlineReplacement() {
        return " ";
    }

    private String pageToString(final Page page) {
        if (page instanceof HtmlPage) {
            final HtmlPage p = (HtmlPage) page;
            return p.asText();
        } else if (page instanceof TextPage) {
            final TextPage p = (TextPage) page;
            return p.getContent();
        } else {
            throw UnknownArgumentException.newInstance(Page.class, page);
        }
    }

    protected RawProxy extractRawProxy(final String host, final String port) {
        if (!Addresses.isIp(host)) {
            return null;
        }
        final InetAddress hostAddr = Addresses.asAddress(host);
        if (hostAddr.isLoopbackAddress() || hostAddr.isSiteLocalAddress()) {
            return null;
        }
        Integer iport;
        if (port != null) {
            try {
                iport = Integer.valueOf(port);
                if (!Addresses.isPort(iport)) {
                    iport = null;
                }
            } catch (final NumberFormatException e) {
                iport = null;
            }
        } else {
            iport = null;
        }
        return ProxyUtil.valueOf(host, iport);
    }
}
