package de.invesdwin.webproxy.geolocation.internal.geonames;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.transaction.annotation.Transactional;

import de.invesdwin.context.ContextProperties;
import de.invesdwin.context.beans.validator.BeanValidator;
import de.invesdwin.context.log.Log;
import de.invesdwin.context.persistence.jpa.PersistenceProperties;
import de.invesdwin.util.concurrent.Threads;
import de.invesdwin.util.lang.string.Strings;
import de.invesdwin.webproxy.geolocation.internal.ADataUpdater;
import de.invesdwin.webproxy.geolocation.internal.GeolocationProperties;
import de.invesdwin.webproxy.geolocation.internal.geonames.persistence.AToponymEntity;
import de.invesdwin.webproxy.geolocation.internal.geonames.persistence.redundance.ToponymRepository;

@Named
@ThreadSafe
public class GeoNamesDataUpdater extends ADataUpdater {

    private static final int TRANSACTION_SIZE = 100000;

    private final File geonamesDataFile = new File(ContextProperties.getCacheDirectory(),
            getClass().getSimpleName() + "_GeoNames.allCountries.txt");

    private final Log log = new Log(this);

    @Inject
    private ToponymRepository toponymRepo;

    public void eventuallyUpdateData() throws Exception {
        //maybe delete incomplete update
        toponymRepo.getInactiveDao().deleteAll();

        final boolean updated = super.eventuallyUpdateData(GeolocationProperties.GEONAMES_DATA_URL, geonamesDataFile);
        final boolean thereIsNoDataInActiveTable = toponymRepo.getActiveDao().isEmpty();
        if (updated || thereIsNoDataInActiveTable) {
            importDataFileIntoInactiveTable(geonamesDataFile);
            log.info("Switching to %s table and emptying %s table.",
                    toponymRepo.newInactiveToponymEntity().getClass().getSimpleName(),
                    toponymRepo.newActiveToponymEntity().getClass().getSimpleName());
            toponymRepo.switchInactiveAndActiveDaoWithEachother();
            /*
             * Lock is anyway only there when inactive table had no data, thus this is no unnecessary expansion of the
             * geonamesservice lock
             */
            toponymRepo.getInactiveDao().deleteAll();
        }
    }

    private void importDataFileIntoInactiveTable(final File daten) throws Exception {
        log.info("Importing new data info %s table.",
                toponymRepo.newInactiveToponymEntity().getClass().getSimpleName());
        final FlatFileItemReader<AToponymEntity> items = GeonamesDataLineMapper.newItemReader(daten);
        try {
            items.open(new ExecutionContext());
            importItemsIntoInactiveTable(items);
        } finally {
            items.close();
        }
    }

    private void importItemsIntoInactiveTable(final ItemReader<AToponymEntity> items) throws Exception {
        int itemsCount = 0;

        final List<AToponymEntity> incompleteItems = new ArrayList<AToponymEntity>();
        itemsCount += importItemsIntoInactiveTableLoop(items, incompleteItems);

        final StringBuilder againIncompleteItemsMessage = new StringBuilder();
        if (incompleteItems.size() > 0) {
            log.warn("%s datasets are incomplete. Now trying to fix these with the nearest neighbours.",
                    incompleteItems.size());
            final ItemReader<AToponymEntity> correctedItems = toponymRepo.getInactiveDao()
                    .tryToCorrectWithNearestNeighbour(incompleteItems);
            final List<AToponymEntity> againIncompleteItems = new ArrayList<AToponymEntity>();
            itemsCount += importItemsIntoInactiveTableNewTx(correctedItems, againIncompleteItems);

            if (againIncompleteItems.size() > 0) {
                againIncompleteItemsMessage.append(againIncompleteItems.size());
                againIncompleteItemsMessage
                        .append(" datasets could not be imported because of the following reasons:\n");
                for (int i = 0; i < againIncompleteItems.size(); i++) {
                    againIncompleteItemsMessage.append(i);
                    againIncompleteItemsMessage.append(": ");
                    againIncompleteItemsMessage
                            .append(BeanValidator.getInstance().validate(againIncompleteItems.get(i)).getMessage());
                    againIncompleteItemsMessage.append("\n");
                }
            }
        }
        log.info("Successfully imported %s new datasets into %s table. %s", itemsCount,
                toponymRepo.newInactiveToponymEntity().getClass().getSimpleName(), againIncompleteItemsMessage);

    }

    private int importItemsIntoInactiveTableLoop(final ItemReader<AToponymEntity> items,
            final List<AToponymEntity> incompleteItems) throws Exception {
        int itemsCount = 0;
        int itemsCountNewTx;
        do {
            itemsCountNewTx = importItemsIntoInactiveTableNewTx(items, incompleteItems);
            Threads.throwIfInterrupted();
            for (int i = 0; i < itemsCountNewTx; i++) {
                itemsCount++;
                if (itemsCount % TRANSACTION_SIZE == 0) {
                    log.info("Import is at %s rows", itemsCount);
                }
            }
        } while (itemsCountNewTx > 0);
        return itemsCount;
    }

    /**
     * Make small transactions to make processing faster.
     */
    @SuppressWarnings("unchecked")
    @Transactional
    private int importItemsIntoInactiveTableNewTx(final ItemReader<AToponymEntity> reader,
            final List<AToponymEntity> incompleteItems) throws Exception {
        AToponymEntity item;
        final int connectionBatchSize = PersistenceProperties
                .getPersistenceUnitContext(toponymRepo.getActiveDao().getPersistenceUnitName())
                .getConnectionBatchSize();
        int itemsCount = 0;
        do {
            item = reader.read();
            if (item != null) {
                /*
                 * Validation must happen outside, because the rollback configuration of the inner transaction cannot be
                 * manipulated from outside. See:
                 * http://stackoverflow.com/questions/3573418/spring-transaction-propagation-issue
                 */
                if (Strings.isNotBlank(item.getCountryCode()) && Strings.isNotBlank(item.getTimeZoneId())) {
                    toponymRepo.getInactiveDao().save(item);
                    itemsCount++;
                    if (itemsCount % connectionBatchSize == 0) {
                        toponymRepo.getInactiveDao().flush();
                        toponymRepo.getInactiveDao().clear();
                    }
                    if (itemsCount >= TRANSACTION_SIZE) {
                        break;
                    }
                } else {
                    incompleteItems.add(item);
                }
            }
        } while (item != null);
        return itemsCount;
    }

}
