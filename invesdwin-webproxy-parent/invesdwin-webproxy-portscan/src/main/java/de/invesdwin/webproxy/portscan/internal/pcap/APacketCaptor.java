package de.invesdwin.webproxy.portscan.internal.pcap;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

import de.invesdwin.context.log.Log;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.concurrent.Executors;
import jpcap.JpcapCaptor;
import jpcap.PacketReceiver;

@ThreadSafe
public abstract class APacketCaptor {

    private final Log log = new Log(this);

    private final ExecutorService executor = Executors.newCachedThreadPool(this.getClass().getSimpleName()); //Unbegrenzt, damit das delayed close keine probleme macht
    private PacketReceiverRunnable receiver;

    protected abstract PacketReceiver getPacketReceiver();

    protected abstract String getPacketFilter();

    public void initialize(final JpcapCaptor pcapCaptor, final InetAddress localBindAddress) throws IOException {
        Assertions.assertThat(receiver).as("close() should be called before reinitialization.").isNull();
        final String pf = getPacketFilter();
        Assertions.assertThat(pf).isNotBlank();
        final String filter = "dst host " + localBindAddress.getHostAddress() + " and " + pf;
        log.debug("Setting filter: " + filter);
        pcapCaptor.setFilter(filter, true);
        pcapCaptor.setNonBlockingMode(false);
        receiver = new PacketReceiverRunnable(pcapCaptor, getPacketReceiver());
        executor.execute(receiver);
    }

    public void close() {
        if (receiver != null) {
            receiver.close();
            receiver = null;
        }
    }

    @Immutable
    private static class PacketReceiverRunnable implements Runnable {

        private final PacketReceiver pr;
        private final JpcapCaptor pcapCaptor;

        PacketReceiverRunnable(@Nonnull final JpcapCaptor pcapCaptor, @Nonnull final PacketReceiver pr) {
            Assertions.assertThat(pr).isNotNull();
            Assertions.assertThat(pcapCaptor).isNotNull();
            this.pcapCaptor = pcapCaptor;
            this.pr = pr;
        }

        @Override
        public void run() {
            try {
                pcapCaptor.loopPacket(-1, pr);
            } finally {
                pcapCaptor.close();
            }
        }

        public void close() {
            //close during loopPacket causes a segmentation fault, thus this workaround
            pcapCaptor.breakLoop();
        }

    }

}
