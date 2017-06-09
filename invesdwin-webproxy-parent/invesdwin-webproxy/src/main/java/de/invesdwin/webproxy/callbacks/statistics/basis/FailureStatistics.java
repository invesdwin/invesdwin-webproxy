package de.invesdwin.webproxy.callbacks.statistics.basis;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import com.google.common.base.Functions;
import com.google.common.collect.Ordering;

@ThreadSafe
public class FailureStatistics {

    @GuardedBy("this")
    private final Map<String, Long> failure_count = new HashMap<String, Long>();

    public synchronized void addFailure(final Throwable failure) {
        final String censoredFailure = censorFailure(failure.toString());
        addFailure(censoredFailure, 1L);
    }

    public synchronized void addFailureStatistics(final FailureStatistics otherFailureStatistics) {
        for (final Entry<String, Long> e : otherFailureStatistics.failure_count.entrySet()) {
            addFailure(e.getKey(), e.getValue());
        }
    }

    private void addFailure(final String censoredFailure, final Long count) {
        final Long previousCount = failure_count.get(censoredFailure);
        if (previousCount == null) {
            failure_count.put(censoredFailure, count);
        } else {
            failure_count.put(censoredFailure, previousCount + count);
        }
    }

    /**
     * Returns the failures in descending order by count.
     * 
     * @see <a
     *      href="http://stackoverflow.com/questions/109383/how-to-sort-a-mapkey-value-on-the-values-in-java">Source</a>
     */
    public synchronized Map<String, Long> getFailuresSortedByCount() {
        final List<String> sortedKeys = Ordering.natural()
                .reverse()
                .onResultOf(Functions.forMap(failure_count))
                .immutableSortedCopy(failure_count.keySet());
        final Map<String, Long> sortedMap = new LinkedHashMap<String, Long>();
        for (final String key : sortedKeys) {
            sortedMap.put(key, failure_count.get(key));
        }
        return sortedMap;
    }

    /**
     * Removes addresses from the failures, so that duplicates are prevented in the sets.
     */
    private String censorFailure(final String failure) {
        final String forUrl_ = " for url: ";
        final String http_ = "http://";
        final String for_ = " for ";

        try {
            final String lowercase = failure.toLowerCase();
            String censored;
            if (lowercase.matches(".*Connect to .* failed.*")) {
                censored = failure.replaceFirst("\\(.*\\)", "(...)");
                censored = failure.replaceFirst("to .* failed.", "to ... failed");
            } else if (lowercase.matches(".*truncated chunk \\(.*\\)")) {
                censored = failure.replaceFirst("\\(.*\\)", "(...)");
            } else if (lowercase.matches(".*connection to .* refused")) {
                censored = failure.replaceFirst("to .* refused", "to ... refused");
            } else if (lowercase.matches(".*connect to .* timed out")) {
                censored = failure.replaceFirst("to .* timed", "to ... timed");
            } else if (lowercase.contains("server returned http response code:") && lowercase.contains(forUrl_)) {
                censored = failure.substring(0, lowercase.indexOf(forUrl_) + forUrl_.length()) + "...";
            } else if (lowercase.matches(".*[0-9]{3}.*" + for_ + ".*")) {
                censored = failure.substring(0, lowercase.indexOf(for_) + for_.length()) + "...";
            } else if (lowercase.contains(http_)) {
                censored = failure.substring(0, failure.indexOf(http_) + http_.length()) + "... ";
            } else {
                censored = failure;
            }

            //Put failure in a single line
            return censored.replaceAll("[\\s]+", " ");
        } catch (final Throwable t) {
            throw new RuntimeException("At failure: " + failure, t);
        }
    }
}
