package de.invesdwin.webproxy.internal.get.string;

import javax.annotation.concurrent.ThreadSafe;
import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

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
        Assertions.assertThat(get.get(new GetStringConfig(), URIs.asUri("https://google.com"))).isNotNull();
    }
}
