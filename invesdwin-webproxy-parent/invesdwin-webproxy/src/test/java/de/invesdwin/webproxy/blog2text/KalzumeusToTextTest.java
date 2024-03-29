package de.invesdwin.webproxy.blog2text;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;

import org.junit.jupiter.api.Test;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import de.invesdwin.context.ContextProperties;
import de.invesdwin.context.integration.ws.registry.RegistryServiceStub;
import de.invesdwin.context.test.ATest;
import de.invesdwin.context.test.TestContext;
import de.invesdwin.util.concurrent.future.Futures;
import de.invesdwin.util.lang.Files;
import de.invesdwin.util.lang.string.Strings;
import de.invesdwin.util.lang.uri.URIs;
import de.invesdwin.webproxy.GetPageConfig;
import de.invesdwin.webproxy.IWebproxyService;
import de.invesdwin.webproxy.broker.contract.BrokerServiceStub;
import jakarta.inject.Inject;

@NotThreadSafe
public class KalzumeusToTextTest extends ATest {

    @Inject
    private IWebproxyService webproxy;

    @Override
    public void setUpContext(final TestContext ctx) throws Exception {
        super.setUpContext(ctx);
        ctx.deactivateBean(RegistryServiceStub.class);
        ctx.deactivateBean(BrokerServiceStub.class);
        //        ctx.deactivate(CommonDirectoriesStub.class);
    }

    @Test
    public void testDownload() throws InterruptedException, IOException {
        int index = 56;

        final String baseUri = "http://www.kalzumeus.com/blog/page/";

        int curPost = 1;
        while (index > 0) {
            final URI request = URIs.asUri(baseUri + index + "/");
            final HtmlPage page = (HtmlPage) Futures
                    .get(webproxy.getPage(new GetPageConfig().setSystemProxyAsFixedProxy(), request));
            final DomElement elementById = page.getElementById("main");
            final List<String> list = new ArrayList<String>();
            final Iterable<DomNode> children = elementById.getChildren();
            if (children != null) {
                for (final DomNode post : children) {
                    final String text = post.asNormalizedText();
                    if (Strings.isNotBlank(text)) {
                        list.add(0, text);
                    }
                }
                for (final String text : list) {
                    Files.writeStringToFile(
                            new File(ContextProperties.getCacheDirectory(), "kalzumeus_" + curPost + ".txt"), text);
                    curPost++;
                }
            } else {
                log.warn(request + ": " + elementById.asNormalizedText());
            }

            index--;
        }
    }
}
