package de.invesdwin.webproxy;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.annotation.concurrent.ThreadSafe;

import org.assertj.core.api.Fail;
import org.junit.jupiter.api.Test;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import de.invesdwin.context.test.ATest;
import de.invesdwin.context.test.TestContext;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.concurrent.future.Futures;
import de.invesdwin.util.error.Throwables;
import de.invesdwin.util.lang.uri.URIs;
import de.invesdwin.webproxy.callbacks.statistics.ConsoleReportStatisticsCallback;
import de.invesdwin.webproxy.internal.WebproxyService;
import jakarta.inject.Inject;

@ThreadSafe
public class WebproxyServiceTest extends ATest {

    private static final String DOWNLOAD_URL = "https://www.wikipedia.org";

    @Inject
    private WebproxyService service;

    @Override
    public void setUpContext(final TestContext ctx) throws Exception {
        super.setUpContext(ctx);
        //        ctx.deactivate(RegistryServiceMock.class);
        //        ctx.deactivate(BrokerServiceMock.class);
    }

    @Test
    public void testDownload() throws InterruptedException, ExecutionException {
        final GetStringConfig config = new GetStringConfig();
        final String dl = service.getString(config, URIs.asUri(DOWNLOAD_URL)).get();
        Assertions.assertThat(dl).isNotNull();
        log.info(dl);
    }

    @Test
    public void testDownloadBusy() throws InterruptedException {
        final GetStringConfig config = new GetStringConfig().setStatisticsCallback(
                new ConsoleReportStatisticsCallback().setLogSessionProgress(true).setLogSessionSummary(true));

        final List<URI> download_uris = new ArrayList<URI>();
        for (int i = 0; i < WebproxyProperties.MAX_PARALLEL_DOWNLOADS + 10; i++) {
            download_uris.add(URIs.asUri(DOWNLOAD_URL));
        }

        for (final String s : Futures.get(service.getString(config, download_uris))) {
            Assertions.assertThat(s).isNotNull();
        }
    }

    @Test
    public void testGetPage() throws IOException, InterruptedException {
        final GetPageConfig config = new GetPageConfig();
        Assertions.assertThat(service.getPage(config, URIs.asUri("http://google.com"))).isNotNull();
    }

    @Test
    public void testClientGetPage() throws FailingHttpStatusCodeException, IOException {
        final WebClient client = service.newWebClient(new GetPageConfig());
        final HtmlPage overviewPage = client.getPage(URIs.asUrl(
                "https://www.ariva.de/dax-index/historische_kurse?boerse_id=12&month=&clean_split=1&clean_payout=1&clean_bezug=1&currency=EUR"));
        Assertions.checkNotNull(overviewPage);
    }

    @Test
    public void testGetPageInvalidPage() throws InterruptedException, ExecutionException {
        final ConsoleReportStatisticsCallback callback = new ConsoleReportStatisticsCallback()
                .setLogSessionProgress(true)
                .setLogSessionSummary(true);
        final GetPageConfig config = new GetPageConfig();
        config.setStatisticsCallback(callback);
        try {
            log.warn(service.getString(config, URIs.asUri("http://subess")).get());
            Fail.fail("Exception expected");
        } catch (final Throwable t) {
            final IOException e = Throwables.getCauseByType(t, IOException.class);
            Assertions.assertThat(e).isNotNull();
        }
    }

}
