package de.invesdwin.webproxy.portscan.internal.pcap;

import javax.annotation.concurrent.ThreadSafe;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.junit.jupiter.api.Test;

import de.invesdwin.context.test.ATest;
import de.invesdwin.util.assertions.Assertions;

@ThreadSafe
@Named
public class PcapConfigurationCheckerTest extends ATest {

    @Inject
    private PcapConfigurationChecker checker;

    @Test
    public void testCheckIcmp() throws InterruptedException {
        Assertions.assertThat(checker.icmpWorks()).isTrue();
    }

    @Test
    public void testCheckSyn() throws InterruptedException {
        Assertions.assertThat(checker.synWorks()).isTrue();
    }

}
