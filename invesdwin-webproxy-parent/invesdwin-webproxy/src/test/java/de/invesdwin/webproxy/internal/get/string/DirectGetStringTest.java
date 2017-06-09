package de.invesdwin.webproxy.internal.get.string;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

import org.junit.Test;

import de.invesdwin.context.test.ATest;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.lang.uri.URIs;
import de.invesdwin.webproxy.GetStringConfig;

@ThreadSafe
public class DirectGetStringTest extends ATest {

    @Inject
    private DirectGetString get;

    @Test
    public void test() throws InterruptedException {
        Assertions.assertThat(get.get(new GetStringConfig(), URIs.asUri("https://subes.dyndns.org/index.php")))
        .isNotNull();
    }
}
