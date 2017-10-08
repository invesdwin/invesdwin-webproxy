package de.invesdwin.webproxy.callbacks.statistics.basis;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.concurrent.ThreadSafe;

import org.junit.jupiter.api.Test;

import de.invesdwin.context.test.ATest;
import de.invesdwin.util.assertions.Assertions;

@ThreadSafe
public class FailureStatisticsTest extends ATest {

    @Test
    public void testGetFailuresSortedByCount() {
        final FailureStatistics s = new FailureStatistics();
        for (int i = 0; i < 10; i++) {
            s.addFailure(new IOException("ten"));
        }
        for (int i = 0; i < 3; i++) {
            s.addFailure(new IOException("three"));
        }
        s.addFailure(new IOException("one"));
        final Map<String, Long> fehler_anzahl = s.getFailuresSortedByCount();
        Assertions.assertThat(fehler_anzahl.size()).isEqualTo(3);
        Assertions.assertThat(fehler_anzahl.get("java.io.IOException: ten")).isEqualTo(10);
        Assertions.assertThat(fehler_anzahl.get("java.io.IOException: three")).isEqualTo(3);
        Assertions.assertThat(fehler_anzahl.get("java.io.IOException: one")).isEqualTo(1);
        final Iterator<Entry<String, Long>> it = fehler_anzahl.entrySet().iterator();
        final Entry<String, Long> ten = it.next();
        Assertions.assertThat(ten.getKey()).isEqualTo("java.io.IOException: ten");
        Assertions.assertThat(ten.getValue()).isEqualTo(10);
        final Entry<String, Long> three = it.next();
        Assertions.assertThat(three.getKey()).isEqualTo("java.io.IOException: three");
        Assertions.assertThat(three.getValue()).isEqualTo(3);
        final Entry<String, Long> one = it.next();
        Assertions.assertThat(one.getKey()).isEqualTo("java.io.IOException: one");
        Assertions.assertThat(one.getValue()).isEqualTo(1);
        Assertions.assertThat(it.hasNext()).isFalse();
    }

}
