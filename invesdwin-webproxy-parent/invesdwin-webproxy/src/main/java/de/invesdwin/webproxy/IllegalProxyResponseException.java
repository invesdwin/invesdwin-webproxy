package de.invesdwin.webproxy;

import java.io.IOException;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Is being thrown as a cause when an illegal response has been found by the ProxyResponseCallback.
 * 
 */
@ThreadSafe
public class IllegalProxyResponseException extends IOException {

    private static final long serialVersionUID = 1L;

    public IllegalProxyResponseException() {
        super();
    }

    public IllegalProxyResponseException(final String message) {
        super(message);
    }

    public IllegalProxyResponseException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public IllegalProxyResponseException(final Throwable cause) {
        super(cause);
    }

}
