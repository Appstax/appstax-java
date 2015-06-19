package com.appstax;

import org.json.JSONObject;

public final class AxUser extends AxObject {

    private String sessionId = null;
    private String username = null;

    public AxUser(JSONObject properties) {
        super("users", properties);
    }

    public AxUser(String username, String sessionId, JSONObject properties) {
        this(properties);
        this.username = username;
        this.sessionId = sessionId;
    }

    public AxUser(String username, String sessionId) {
        this(username, sessionId, new JSONObject());
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getUsername() {
        return username;
    }

}
