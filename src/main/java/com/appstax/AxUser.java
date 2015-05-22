package com.appstax;

public final class AxUser {

    private String sessionId = null;
    private String username = null;
    private AxObject object = null;

    protected AxUser(String username, String session, AxObject object) {
        this.username = username;
        this.sessionId = session;
        this.object = object;
    }

    protected AxObject getObject() {
        return this.object;
    }

    public String getUsername() {
        return this.username;
    }

    public String getSessionId() {
        return this.sessionId;
    }

    public void put(String key, Object val) {
        this.object.put(key, val);
    }

    public Object get(String key) {
        return this.object.get(key);
    }

}
