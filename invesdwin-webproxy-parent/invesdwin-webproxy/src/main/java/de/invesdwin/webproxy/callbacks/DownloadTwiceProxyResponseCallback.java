package de.invesdwin.webproxy.callbacks;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.concurrent.ThreadSafe;

import de.invesdwin.util.assertions.Assertions;
import de.invesdwin.util.collections.delegate.ADelegateCollection;
import de.invesdwin.util.collections.loadingcache.ALoadingCache;

/**
 * Here a default strategy for response verification is implemented. It simply downloads the page via multiple proxies
 * and checks that the payload is the same.
 * 
 * @author subes
 * 
 */
@ThreadSafe
public class DownloadTwiceProxyResponseCallback extends AProxyResponseCallback {

    private final ALoadingCache<URI, LastResponses> uri_lastResponses = new ALoadingCache<URI, LastResponses>() {
        @Override
        protected LastResponses loadValue(final URI key) {
            return new LastResponses();
        }
    };
    private int maxLastResponsesToConsider = 3;

    @Override
    public final boolean isAnotherDownloadNeededForVerification(final URI uri, final String stringResponse,
            final Object originalResponse) {
        final String responsePayload = extractResponsePayload(uri, stringResponse, originalResponse);
        if (responsePayload == null) {
            return true;
        }

        final LastResponses lastResponses = uri_lastResponses.get(uri);
        if (lastResponses.contains(responsePayload)) {
            //validation succeeded, thus ok!
            uri_lastResponses.remove(uri);
            return false;
        } else {
            //either this is the first try -> try to download again for validation
            //or the response differ -> try again with the current response kept in memory for validation
            lastResponses.add(responsePayload);
            uri_lastResponses.put(uri, lastResponses);
            //anyway we want a new proxy to validate the response
            return true;
        }
    };

    /**
     * Null may not be returned here normally, because this always counts as a retry being needed. Though on interrupt
     * null may be returned here, because with this a retry happens that fails on an interrupt itself and thus aborts.
     */
    public String extractResponsePayload(final URI uri, final String stringResponse, final Object originalResponse) {
        return stringResponse;
    }

    /**
     * Default is 3. This only counts as the number of previous responses that should be kept in memory. It will still
     * count a response as valid, if it matches one of those.
     */
    public void setMaxLastResponsesToConsider(final int maxLastResponsesToConsider) {
        Assertions.assertThat(maxLastResponsesToConsider).isGreaterThan(0);
        this.maxLastResponsesToConsider = maxLastResponsesToConsider;
    }

    public int getMaxLastResponsesToConsider() {
        return maxLastResponsesToConsider;
    }

    private final class LastResponses extends ADelegateCollection<String> {

        @Override
        protected Collection<String> newDelegate() {
            return new ArrayList<String>();
        }

        @Override
        public boolean isAddAllowed(final String e) {
            if (size() > maxLastResponsesToConsider) {
                remove(iterator().next());
            }
            return super.isAddAllowed(e);
        }

    }

}
