package com.appstax;

public final class AppstaxUser {

    private String sessionId = null;
    private String username = null;
    private AppstaxObject object = null;

    protected AppstaxUser(String username, String session, AppstaxObject object) {
        this.username = username;
        this.sessionId = session;
        this.object = object;
    }

    protected AppstaxObject getObject() {
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
