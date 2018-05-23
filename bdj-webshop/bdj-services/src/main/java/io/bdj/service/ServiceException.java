package io.bdj.service;

/**
 *
 */
public class ServiceException extends Exception {

    public ServiceException() {

        super();
    }

    public ServiceException(final String message) {

        super(message);
    }

    public ServiceException(final String message, final Throwable cause) {

        super(message, cause);
    }

    public ServiceException(final Throwable cause) {

        super(cause);
    }

    protected ServiceException(final String message,
                               final Throwable cause,
                               final boolean enableSuppression,
                               final boolean writableStackTrace) {

        super(message, cause, enableSuppression, writableStackTrace);
    }
}
