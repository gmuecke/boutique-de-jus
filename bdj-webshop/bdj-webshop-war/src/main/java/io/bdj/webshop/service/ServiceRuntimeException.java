package io.bdj.webshop.service;

/**
 *
 */
public class ServiceRuntimeException extends RuntimeException {

    public ServiceRuntimeException() {

        super();
    }

    public ServiceRuntimeException(final String message) {

        super(message);
    }

    public ServiceRuntimeException(final String message, final Throwable cause) {

        super(message, cause);
    }

    public ServiceRuntimeException(final Throwable cause) {

        super(cause);
    }

    protected ServiceRuntimeException(final String message,
                                      final Throwable cause,
                                      final boolean enableSuppression,
                                      final boolean writableStackTrace) {

        super(message, cause, enableSuppression, writableStackTrace);
    }
}
