package de.invesdwin.webproxy.internal.get.page;

import java.util.concurrent.ExecutionException;

import javax.annotation.concurrent.ThreadSafe;
import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import de.invesdwin.context.test.ATest;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.lang.uri.URIs;
import de.invesdwin.webproxy.GetPageConfig;

@ThreadSafe
public class DirectGetPageTest extends ATest {

    @Inject
    private DirectGetPage get;

    @Test
    public void test() throws InterruptedException, ExecutionException {
        Assertions.assertThat(get.get(new GetPageConfig(), URIs.asUri("https://google.com")).get()).isNotNull();
        Assertions.assertThat(get.get(new GetPageConfig(), URIs.asUri(
                "https://www.ariva.de/dax-index/historische_kurse?boerse_id=12&month=&clean_split=1&clean_payout=1&clean_bezug=1&currency=EUR"))
                .get()).isNotNull();
    }
}
