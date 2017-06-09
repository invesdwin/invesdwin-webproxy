package de.invesdwin.webproxy.portscan.internal.pcap;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Test;

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
