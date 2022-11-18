package de.invesdwin.webproxy.geolocation.internal.geonames;

import java.io.File;
import java.util.List;

import javax.annotation.concurrent.ThreadSafe;
import jakarta.inject.Inject;

import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.core.io.FileSystemResource;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.webproxy.geolocation.internal.geonames.persistence.AToponymEntity;
import de.invesdwin.webproxy.geolocation.internal.geonames.persistence.ClassificationUtil;
import de.invesdwin.webproxy.geolocation.internal.geonames.persistence.redundance.ToponymRepository;

/**
 * This is the format of the file:
 * <ol>
 * <li>geonameid : integer id of record in geonames database</li>
 * <li>name : name of geographical point (utf8) varchar(200)</li>
 * <li>asciiname : name of geographical point in plain ascii characters, varchar(200)</li>
 * <li>alternatenames : alternatenames, comma separated varchar(5000)</li>
 * <li>latitude : latitude in decimal degrees (wgs84)</li>
 * <li>longitude : longitude in decimal degrees (wgs84)</li>
 * <li>feature class : see http://www.geonames.org/export/codes.html, char(1)</li>
 * <li>feature code : see http://www.geonames.org/export/codes.html, varchar(10)</li>
 * <li>country code : ISO-3166 2-letter country code, 2 characters</li>
 * <li>cc2 : alternate country codes, comma separated, ISO-3166 2-letter country code, 60 characters</li>
 * <li>admin1 code : fipscode (subject to change to iso code), see exceptions below, see file admin1Codes.txt for
 * display names of this code; varchar(20)</li>
 * <li>admin2 code : code for the second administrative division, a county in the US, see file admin2Codes.txt;
 * varchar(80)</li>
 * <li>admin3 code : code for third level administrative division, varchar(20)</li>
 * <li>admin4 code : code for fourth level administrative division, varchar(20)</li>
 * <li>population : bigint (8 byte int)</li>
 * <li>elevation : in meters, integer</li>
 * <li>gtopo30 : average elevation of 30'x30' (ca 900mx900m) area in meters, integer</li>
 * <li>timezone : the timezone id (see file timeZone.txt)</li>
 * <li>modification date : date of last modification in yyyy-MM-dd format</li>
 * </ol>
 */
@ThreadSafe
@Configurable
public final class GeonamesDataLineMapper implements LineMapper<AToponymEntity> {

    @Inject
    private ToponymRepository toponymRepo;

    private GeonamesDataLineMapper() {}

    @Override
    public AToponymEntity mapLine(final String line, final int lineNumber) throws Exception {
        final List<String> lineParts = ImmutableList.copyOf(Splitter.on("\t").split(line));
        Assertions.assertThat(lineParts.size())
        .as("Invalid split length [%s] for line [%s]", lineParts.size(), line)
        .isEqualTo(19);
        final AToponymEntity ent = toponymRepo.newInactiveToponymEntity();
        final String sLatitude = lineParts.get(4);
        final Float latitude = Float.valueOf(sLatitude);
        ent.setLatitude(latitude);
        final String sLongitude = lineParts.get(5);
        final Float longitude = Float.valueOf(sLongitude);
        ent.setLongitude(longitude);
        final String name = lineParts.get(2); //asciiname should be better
        ent.setLocationName(name);
        final String countryCode = lineParts.get(8);
        ent.setCountryCode(countryCode);
        final String timezone = lineParts.get(17);
        ent.setTimeZoneId(timezone);
        final Integer klassifikation = ClassificationUtil.berechneKlassifikation(ent);
        ent.setClassification(klassifikation);
        return ent;
    }

    public static FlatFileItemReader<AToponymEntity> newItemReader(final File daten) throws Exception {
        final FlatFileItemReader<AToponymEntity> items = new FlatFileItemReader<AToponymEntity>();
        items.setResource(new FileSystemResource(daten));
        items.setSaveState(false);
        items.setLineMapper(new GeonamesDataLineMapper());
        items.afterPropertiesSet();
        return items;
    }

}
