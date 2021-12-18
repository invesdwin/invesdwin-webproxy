package de.invesdwin.webproxy.broker.internal.persistence;

import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import javax.validation.ConstraintViolationException;

import org.assertj.core.api.Fail;
import org.junit.jupiter.api.Test;

import de.invesdwin.context.integration.network.RandomIpGenerator;
import de.invesdwin.context.persistence.jpa.test.APersistenceTest;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.error.Throwables;
import de.invesdwin.util.time.date.FDate;
import de.invesdwin.util.time.date.FTimeUnit;
import de.invesdwin.webproxy.broker.contract.schema.ProxyQuality;
import de.invesdwin.webproxy.broker.contract.schema.ProxyType;
import de.invesdwin.webproxy.broker.internal.BrokerProperties;

@ThreadSafe
public class ProxyDaoTest extends APersistenceTest {

    @Inject
    private ProxyDao dao;

    @Override
    public void tearDown() {
        clearAllTables();
    }

    @Test
    public void testValidation() {
        final ProxyEntity p = new ProxyEntity();
        p.setHost("asd");
        p.setPort(-1);
        p.setType(ProxyType.HTTP);
        p.setLastSuccessful(new FDate());
        try {
            dao.save(p);
            Fail.fail("Exception expected");
        } catch (final Throwable t) {
            Assertions.assertThat(Throwables.isCausedByType(t, ConstraintViolationException.class)).isTrue();
        }
    }

    @Test
    public void testWriteOrUpdate() {
        ProxyEntity pVorher = new ProxyEntity();
        pVorher.setHost("127.0.0.1");
        pVorher.setPort(80);
        pVorher.setCountryCode("DE");
        pVorher.setQuality(ProxyQuality.ANONYMOUS);
        pVorher.setType(ProxyType.HTTP);
        pVorher.setTimeZoneId(TimeZone.getDefault().getID());
        dao.writeOrUpdate(pVorher);

        final ProxyEntity pAktualisiert = new ProxyEntity();
        pAktualisiert.mergeFrom(pVorher);
        pAktualisiert.setType(ProxyType.SOCKS);
        pAktualisiert.setQuality(ProxyQuality.TRANSPARENT);
        dao.writeOrUpdate(pAktualisiert);

        pVorher = dao.findOne(pVorher);
        Assertions.assertThat(pVorher.getType()).isEqualTo(pAktualisiert.getType());
        Assertions.assertThat(pVorher.getQuality()).isEqualTo(pAktualisiert.getQuality());
    }

    @Test
    public void testReadUsedPortsSorted() {
        for (int i = 2; i <= 5; i++) {
            for (int j = 0; j < i; j++) {
                final ProxyEntity p = ProxyEntity.valueOf("127.0.0.1" + i + "" + j, i, ProxyType.HTTP,
                        ProxyQuality.TRANSPARENT, Locale.getDefault().getCountry(), TimeZone.getDefault().getID());
                p.setLastSuccessful(new FDate());
                dao.save(p);
            }
        }
        final ProxyEntity p = ProxyEntity.valueOf("127.0.0.1" + 7 + "" + 7, 7, ProxyType.HTTP, ProxyQuality.TRANSPARENT,
                Locale.getDefault().getCountry(), TimeZone.getDefault().getID());
        p.setLastSuccessful(new FDate());
        dao.save(p);

        final List<Integer> l = dao.readUsedPortsSorted(Integer.MAX_VALUE);
        int i = 0;
        for (int port = 5; port >= 2; port--) {
            Assertions.assertThat(l.get(i)).isEqualTo(port);
            i++;
        }
        Assertions.assertThat(l.get(l.size() - 1)).isEqualTo(7);
        Assertions.assertThat(dao.readUsedPortsSorted(1).size()).isEqualTo(1);
    }

    @Test
    public void testGetDowntimeToleranceExceededProxies() {
        for (int i = 1; i <= 10; i++) {
            final String host = RandomIpGenerator.getRandomIp().getHostAddress();
            final ProxyType type;
            if (i % 2 == 0) {
                type = ProxyType.HTTP;
            } else {
                type = ProxyType.SOCKS;
            }
            final ProxyEntity p = ProxyEntity.valueOf(host, i, type, ProxyQuality.TRANSPARENT,
                    Locale.getDefault().getCountry(), TimeZone.getDefault().getID());
            if (i % 2 == 0) {
                p.setLastSuccessful(new FDate().addMilliseconds(
                        -BrokerProperties.PROXY_DOWNTIME_TOLERANCE.intValue(FTimeUnit.MILLISECONDS) * 2));
            } else {
                p.setLastSuccessful(new FDate());
            }

            if (!dao.writeOrUpdate(p)) {
                i--;
            }
        }
        Assertions.assertThat(dao.readDowntimeToleranceExceededProxies().size()).isEqualTo(5);
    }
}
