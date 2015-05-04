package com.appstax.exceptions;

public final class AppstaxRequestException extends AppstaxException {

    public AppstaxRequestException(String message) {
        super(message, null);
    }

    public AppstaxRequestException(String message, Throwable e) {
        super(message, e);
    }

}
