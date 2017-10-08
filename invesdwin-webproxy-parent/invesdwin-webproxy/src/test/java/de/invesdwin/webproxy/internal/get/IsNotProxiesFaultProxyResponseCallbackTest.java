package de.invesdwin.webproxy.internal.get;

import java.io.IOException;
import java.io.StringWriter;

import javax.annotation.concurrent.ThreadSafe;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import de.invesdwin.context.test.ATest;
import de.invesdwin.util.assertions.Assertions;

@ThreadSafe
public class IsNotProxiesFaultProxyResponseCallbackTest extends ATest {

    @Test
    public void testIsValidResult() throws IOException {
        final StringWriter sw = new StringWriter();
        IOUtils.copy(this.getClass().getResourceAsStream("gurufocus.html"), sw);
        Assertions.assertThat(
                IsNotProxiesFaultProxyResponseCallback.INSTANCE.isValidResponse(null, sw.toString(), sw.toString()))
                .isTrue();
    }

}
