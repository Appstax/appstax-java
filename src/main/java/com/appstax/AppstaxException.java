package com.appstax;

final class AppstaxException extends RuntimeException {

    private int status;
    private String id;
    private String code;
    private String message;

    public AppstaxException(int status, String id, String code, String message) {
        super(message, null);
        this.status = status;
        this.id = id;
        this.code = code;
        this.message = message;
    }

    public AppstaxException(String message, Throwable e) {
        super(message, e);
    }

    public AppstaxException(String message) {
        super(message, null);
    }

    public int getStatus() {
        return status;
    }

    public String getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

}
