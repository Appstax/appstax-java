package com.appstax;

import org.json.JSONObject;

public final class AxUser extends AxObject {

    private String sessionId = null;
    private String username = null;

    protected AxUser(AxClient client, JSONObject properties) {
        super(client, "users", properties);
    }

    protected AxUser(AxClient client, String username, String sessionId, JSONObject properties) {
        this(client, properties);
        this.username = username;
        this.sessionId = sessionId;
    }

    protected AxUser(AxClient client, String username, String sessionId) {
        this(client, username, sessionId, new JSONObject());
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getUsername() {
        return username;
    }

}
