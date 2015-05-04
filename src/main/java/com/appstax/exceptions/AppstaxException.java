package com.appstax.exceptions;

public class AppstaxException extends RuntimeException {

    public AppstaxException(String message) {
        super(message, null);
    }

    public AppstaxException(String message, Throwable e) {
        super(message, e);
    }

}
