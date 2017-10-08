package de.invesdwin.webproxy.internal.get.page;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

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
    public void test() throws InterruptedException {
        Assertions.assertThat(get.get(new GetPageConfig(), URIs.asUri("https://subes.dyndns.org/index.php")))
        .isNotNull();
    }
}
