package com.appstax.exceptions;

public final class AppstaxConnectionException extends AppstaxException {

    public AppstaxConnectionException(String message) {
        super(message, null);
    }

    public AppstaxConnectionException(String message, Throwable e) {
        super(message, e);
    }

}
