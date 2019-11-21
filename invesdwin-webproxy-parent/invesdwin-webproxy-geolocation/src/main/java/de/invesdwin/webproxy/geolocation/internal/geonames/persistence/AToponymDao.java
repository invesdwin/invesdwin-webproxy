package de.invesdwin.webproxy.geolocation.internal.geonames.persistence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.concurrent.ThreadSafe;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Joiner;

import de.invesdwin.context.beans.hook.IStartupHook;
import de.invesdwin.context.log.Log;
import de.invesdwin.context.persistence.jpa.ConnectionDialect;
import de.invesdwin.context.persistence.jpa.PersistenceProperties;
import de.invesdwin.context.persistence.jpa.api.dao.ADao;
import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.lang.Strings;
import de.invesdwin.webproxy.geolocation.internal.GeolocationProperties;

@ThreadSafe
public abstract class AToponymDao<E extends AToponymEntity> extends ADao<E> implements IStartupHook {

    private final Log log = new Log(this);

    /**
     * Calculating the Euclidian distances and after that determining the 1-nearest-neighbour.
     * 
     * <pre>
     * d(lat, long) = sqrt( ( lat1 - lat1 ) ^2 + ( long2 - long2 ) ^2 )
     * </pre>
     * 
     * We spare us the sqrt because it does not change the order. It just decreases performance.
     * 
     * @see <a href="http://stackoverflow.com/questions/2064977/jpql-to-get-nearest-records-by-latitude-and-longitude">
     *      Source</a>
     */
    @SuppressWarnings("unchecked")
    public E findNearestNeighbour(final float latitude, final float longitude) {

        final int startClassification = ClassificationUtil.calculateClassification(latitude, longitude);
        final String paramLatitude = AToponymEntity_.latitude.getName();
        final String diffLatitude = "( :" + paramLatitude + " - " + AToponymEntity_.latitude.getName() + " )";
        final String diffLatitudeExp2 = "( " + diffLatitude + " * " + diffLatitude + " )";
        final String paramLongitude = AToponymEntity_.longitude.getName();
        final String diffLongitude = "( :" + paramLongitude + " - " + AToponymEntity_.longitude.getName() + " )";
        final String diffLongitudeExp2 = "( " + diffLongitude + " * " + diffLongitude + " )";
        final String distance = diffLatitudeExp2 + " + " + diffLongitudeExp2;
        final String replaceClassifications = "{classifications}";

        final String selectTemplate = "SELECT e, " + distance + " AS distance FROM " + getGenericType().getName()
                + " e WHERE " + AToponymEntity_.classification.getName() + " IN (" + replaceClassifications
                + ") ORDER BY distance ASC";

        E nearestNeighbour = (E) null;
        for (int stepsOutside = 0; nearestNeighbour == null; stepsOutside++) {
            Assertions
                    .assertThat(startClassification - stepsOutside >= 1
                            || startClassification + stepsOutside <= GeolocationProperties.CLASSIFICATIONS_PER_AXIS)
                    .as("Classification limits have been exceeded on both sides! Maybe the table is empty so that no locations can be found with classifications?")
                    .isTrue();
            final String classifications = Joiner.on(",")
                    .join(ClassificationUtil.calculateSurroundingClassifications(startClassification, stepsOutside));

            final List<Object[]> res = getEntityManager()
                    .createQuery(selectTemplate.replace(replaceClassifications, classifications))
                    .setMaxResults(1)
                    .setParameter(paramLatitude, latitude)
                    .setParameter(paramLongitude, longitude)
                    .getResultList();
            if (res.size() > 0) {
                Assertions.assertThat(res.size()).isEqualTo(1);
                nearestNeighbour = (E) res.get(0)[0];
            }
        }
        return nearestNeighbour;
    }

    public ItemReader<E> tryToCorrectWithNearestNeighbour(final List<E> incompleteItems) {
        /*
         * Cache results so that lookups are faster, the cache can have a maximum of the size of number of
         * classification, thus no possible memory leak
         */
        final Map<Integer, E> classifications_nearestNeighbour = new HashMap<Integer, E>();
        int countCacheHits = 0;
        int countDbHits = 0;
        for (final E incompleteItem : incompleteItems) {
            E nearestNeighbour = classifications_nearestNeighbour.get(incompleteItem.getClassification());
            if (nearestNeighbour == null) {
                nearestNeighbour = findNearestNeighbour(incompleteItem.getLatitude(), incompleteItem.getLongitude());
                classifications_nearestNeighbour.put(nearestNeighbour.getClassification(), nearestNeighbour);
                countDbHits++;
            } else {
                countCacheHits++;
            }
            if (Strings.isBlank(incompleteItem.getCountryCode())) {
                incompleteItem.setCountryCode(nearestNeighbour.getCountryCode());
            }
            if (Strings.isBlank(incompleteItem.getTimeZoneId())) {
                incompleteItem.setTimeZoneId(nearestNeighbour.getTimeZoneId());
            }
        }
        log.info("Could correct %s data sets where %s were cache hits and %s came from the database.",
                countCacheHits + countDbHits, countCacheHits, countDbHits);
        return new ListItemReader<E>(incompleteItems);
    }

    @Transactional
    @Override
    public void startup() throws Exception {
        if (PersistenceProperties.getPersistenceUnitContext(getPersistenceUnitName())
                .getConnectionDialect() == ConnectionDialect.MYSQL && isEmpty() && getOtherDao().isEmpty()) {
            getEntityManager()
                    .createNativeQuery(
                            "alter table " + getGenericType().getSimpleName() + " ENGINE=InnoDB, ROW_FORMAT=COMPRESSED")
                    .executeUpdate();
        }
    }

    protected abstract AToponymDao<?> getOtherDao();

}
