package de.invesdwin.webproxy.geolocation.internal.geonames.persistence;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.concurrent.Immutable;

import de.invesdwin.webproxy.geolocation.internal.GeolocationProperties;

@Immutable
public final class ClassificationUtil {

    public static final int MIN_CLASSIFICATIONS = 1;
    public static final int MAX_CLASSIFICATIONS = (int) Math.pow(GeolocationProperties.CLASSIFICATIONS_PER_AXIS, 2);
    private static final float LONGITUDE_PER_CLASSIFICATION = 360 / GeolocationProperties.CLASSIFICATIONS_PER_AXIS;
    private static final float LATITUDE_PER_CLASSIFICATION = 180 / GeolocationProperties.CLASSIFICATIONS_PER_AXIS;

    private ClassificationUtil() {}

    public static int berechneKlassifikation(final AToponymEntity ent) {
        return calculateClassification(ent.getLatitude(), ent.getLongitude());
    }

    public static int calculateClassification(final float latitude, final float longitude) {
        final int longitudeClassification = (int) (((longitude + 180) / LONGITUDE_PER_CLASSIFICATION) + 1);
        final int latitudeClassification = (int) (((latitude + 90) / LATITUDE_PER_CLASSIFICATION) + 1);
        return keepClassificationLimits(latitudeClassification * longitudeClassification);
    }

    /**
     * Calculates an empty square with classifications around the start classification.
     */
    public static Set<Integer> calculateSurroundingClassifications(final int startClassification, final int stepsOutside) {
        final Set<Integer> classifications = new HashSet<Integer>();
        if (stepsOutside == 0) {
            classifications.add(startClassification);
        } else {
            final int left = startClassification - stepsOutside;
            final int right = startClassification + stepsOutside;
            final int horizontalSteps = stepsOutside * GeolocationProperties.CLASSIFICATIONS_PER_AXIS;
            final int leftTop = keepClassificationLimits(left - horizontalSteps);
            final int leftBottom = keepClassificationLimits(left + horizontalSteps);
            final int rightTop = keepClassificationLimits(right - horizontalSteps);
            final int rightBottom = keepClassificationLimits(right + horizontalSteps);
            classifications.addAll(calculateHorizontalClassificationsBetween(leftTop, rightTop));
            classifications.addAll(calculateHorizontalClassificationsBetween(leftBottom, rightBottom));
            classifications.addAll(calculateVerticalClassificationsBetween(leftTop, leftBottom));
            classifications.addAll(calculateVerticalClassificationsBetween(rightTop, rightBottom));
        }
        return classifications;
    }

    private static int keepClassificationLimits(final int theoretischeKlassifikation) {
        if (theoretischeKlassifikation > MAX_CLASSIFICATIONS) {
            return MAX_CLASSIFICATIONS;
        } else if (theoretischeKlassifikation < MIN_CLASSIFICATIONS) {
            return MIN_CLASSIFICATIONS;
        } else {
            return theoretischeKlassifikation;
        }
    }

    private static Set<Integer> calculateHorizontalClassificationsBetween(final int left, final int right) {
        final Set<Integer> classificationsBetween = new HashSet<Integer>();
        for (int k = left; k <= right; k++) {
            classificationsBetween.add(k);
        }
        return classificationsBetween;
    }

    private static Set<Integer> calculateVerticalClassificationsBetween(final int top, final int bottom) {
        final Set<Integer> classificationsBetween = new HashSet<Integer>();
        for (int k = top; k <= bottom; k += GeolocationProperties.CLASSIFICATIONS_PER_AXIS) {
            classificationsBetween.add(k);
        }
        return classificationsBetween;
    }
}
