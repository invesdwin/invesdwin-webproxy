package de.invesdwin.webproxy;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

import org.assertj.core.api.Fail;
import org.junit.Test;

import de.invesdwin.context.test.ATest;
import de.invesdwin.context.test.TestContext;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.concurrent.Futures;
import de.invesdwin.util.error.Throwables;
import de.invesdwin.util.lang.uri.URIs;
import de.invesdwin.webproxy.callbacks.statistics.ConsoleReportStatisticsCallback;
import de.invesdwin.webproxy.internal.WebproxyService;

@ThreadSafe
public class WebproxyServiceTest extends ATest {

    private static final String DOWNLOAD_URL = "http://de.finance.yahoo.com/d/quotes.csv?s=IL0011076630.HK+US68243Q1067.HK+AT0000652011.HK+DE000TACL107.HK+US32037L1035.HK+DE0006055007.HK+DE000A0HL8N9.HK+DE0005087506.HK+KYG884931042.HK+CH0042232998.HK+DE000A0STTV6.HK+US8855351040.HK+AU000000TDO8.HK+GB00B1YW4409.HK+US88579Y1010.HK+US88580F1093.HK+CH0042821089.HK+US88575Y1055.HK+DE0005167902.HK+DE0005753818.HK+CH0043754214.HK+US3508391063.HK+AU000000FCS0.HK+BE0003888089.HK+GB0006640972.HK+US3168271043.HK+GI000A0F6407.HK+US2829141009.HK+US65440K1060.HK+FI0009801310.HK+DE000A0BVVK7.HK+JP3166900005.HK+GRS083003012.HK+DE0006301807.HK+DE000A0F5WM7.HK+US3156161024.HK+AT0000785407.HK+SE0000950636.HK+US3030392001.HK+US3030751057.HK+ES0134950F36.HK+DE000A0MW975.HK+CA3036231023.HK+US3036981047.HK+US3037261035.HK+CA3039011026.HK+BG2100036073.HK+BG2100036057.HK+BG1100042057.HK+BG2100030068.HK+US3055601047.HK+FR0000053142.HK+AU000000FCN1.HK+CA3060711015.HK+US3061371007.HK+DE0005752307.HK+GB0001861599.HK+FK00B030JM18.HK+US3070001090.HK+JP3802600001.HK+CA30710P1027.HK+JP3802400006.HK+KYG3307Z1090.HK+US3073251007.HK+CA3069051009.HK+CA30739P1099.HK+DE0006051923.HK+GB0033032904.HK+USU947591041.HK+US3119001044.HK+IT0001423562.HK+FR0000121147.HK+BMG0639G1481.HK+BG1100035986.HK+BG11FARUAT13.HK+BG11FASIAT18.HK+XS0423724987.HK+DE0006889017.HK+DE0006888571.HK+DE0006888597.HK+DE0006889025.HK+DE0006888589.HK+CH0034060654.HK+BMG334731020.HK+US31309A1007.HK+CA30246X1087.HK+US3134003017.HK+US3137472060.HK+US3135861090.HK+US3142111034.HK+US31428X1063.HK+DE000A0DRW95.HK+US30241L1098.HK+CH0009320091.HK+US31430F1012.HK+US3143081079.HK+AU000000FLX1.HK+US3143471056.HK+DE0005767909.HK+AU000000FRS8.HK+US3152931008.HK+GB00B1XH2C03.HK+CH0038903107.HK+BMG3435X1092.HK+ES0136463017.HK+DE0001698843.HK+DE0001698868.HK+DE0006888498.HK+DE0001698850.HK+DE0006888480.HK+DE0006888472.HK+DE0007202376.HK+DE0007202368.HK+DE0006889116.HK+DE0007202632.HK+DE0007202657.HK+DE0006889090.HK+DE0007202624.HK+DE0007202640.HK+DE0007202384.HK+DE0007202392.HK+DE0007202616.HK+DE0006889124.HK+DE0006889314.HK+DE0007202814.HK+DE0007202830.HK+DE0007202780.HK+DE0006889306.HK+DE0007202822.HK+DE0007202848.HK+DE0007202798.HK+DE0007202855.HK+DE0006889322.HK+IT0001976403.HK+IT0001976411.HK+IT0001976429.HK+CH0044440508.HK+GB00B1FMH067.HK+US3157461076.HK+US31620R1059.HK+US31620M1062.HK+DE000A0MKYF1.HK+GB0004510409.HK+CA3169022041.HK+US3165701000.HK+DE0005772206.HK+US3167731005.HK+CH0035089793.HK+BG11FIPLAT18.HK+GB00B0744359.HK+FR0000037947.HK+BG2100022057.HK+KYG343791003.HK+TRAFINBN91N3.HK+CH0032140961.HK+CA31771G1063.HK+US31787Y1091.HK+SE0000422107.HK+US31787A5074.HK+US3179231002.HK+US31769V1070.HK+US31769V2060.HK+IT0003856405.HK+FI0009003230.HK+FI0009003644.HK+BMG343101058.HK+AU000000FSE6.HK+CA3183181021.HK+US3184571087.HK+US3185223076.HK+AU000000FAR6.HK+US31942R2058.HK+US31942D1072.HK+US31946M1036.HK+CA3203771041.HK+US3205171057.HK+US32054K1034.HK+BG1100106050.HK+CA3207281088.HK+CA32076V1031.HK+US3207711082.HK+CA32111C1023.HK+US3211291089.HK+CA33582W1068.HK+BMG348041077.HK+CA3359371083.HK+US33610F1093.HK+CA3359341052.HK+SG1U27933225.HK+US3363121035.HK+SG1U66934613.HK+US3364331070.HK+CA33647K2048.HK+CNE100000320.HK+CH0105201963.HK+CA33744R1029.HK+US3379321074.HK+GB0003452173.HK+ZAE000066304.HK+CH0039011991.HK&f=z9s";

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
        final GetStringConfig config = new GetStringConfig().withStatisticsCallback(
                new ConsoleReportStatisticsCallback().withLogSessionProgress(true).withLogSessionSummary(true));

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
        Assertions.assertThat(service.getPage(config, URIs.asUri("http://subes.dyndns.org"))).isNotNull();
    }

    @Test
    public void testGetPageInvalidPage() throws InterruptedException, ExecutionException {
        final ConsoleReportStatisticsCallback callback = new ConsoleReportStatisticsCallback()
                .withLogSessionProgress(true).withLogSessionSummary(true);
        final GetPageConfig config = new GetPageConfig();
        config.withStatisticsCallback(callback);
        try {
            log.warn(service.getString(config, URIs.asUri("http://subess")).get());
            Fail.fail("Exception expected");
        } catch (final Throwable t) {
            final IOException e = Throwables.getCauseByType(t, IOException.class);
            Assertions.assertThat(e).isNotNull();
        }
    }

}
