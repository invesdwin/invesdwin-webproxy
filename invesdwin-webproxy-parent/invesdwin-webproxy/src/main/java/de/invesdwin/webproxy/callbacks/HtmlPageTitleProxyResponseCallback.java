package de.invesdwin.webproxy.callbacks;

import java.net.URI;
import java.util.concurrent.ExecutionException;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import de.invesdwin.context.integration.retry.Retry;
import de.invesdwin.context.integration.retry.RetryLaterRuntimeException;
import de.invesdwin.context.log.error.Err;
import de.invesdwin.webproxy.GetPageConfig;
import de.invesdwin.webproxy.IWebproxyService;

/**
 * Checks by title if the the response is correct.
 * 
 * @author subes
 * 
 */
@ThreadSafe
@Configurable
public class HtmlPageTitleProxyResponseCallback extends AProxyResponseCallback implements InitializingBean {

    protected String title;
    private final URI uri;

    @Inject
    private IWebproxyService webproxy;

    public HtmlPageTitleProxyResponseCallback(final URI uri) {
        this.uri = uri;
    }

    @Override
    @Retry
    public void afterPropertiesSet() {
        try {
            final HtmlPage page = (HtmlPage) webproxy.getPage(new GetPageConfig(), uri).get();
            title = page.getTitleText();
        } catch (final InterruptedException e) {
            throw Err.process(e);
        } catch (final ExecutionException e) {
            throw new RetryLaterRuntimeException("Title download failed!", e);
        }
    }

    @Override
    public boolean isValidResponse(final URI uri, final String stringResponse, final Object originalResponse) {
        return stringResponse != null && stringResponse.contains(title);
    }

}
