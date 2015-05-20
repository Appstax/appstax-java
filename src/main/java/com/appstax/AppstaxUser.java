package com.appstax;

import org.json.JSONObject;

public final class AppstaxUser {

    private static final String KEY_SESSIONID = "sysSessionId";
    private static final String KEY_USERNAME = "sysUsername";
    private static final String KEY_PASSWORD = "sysPassword";
    private static final String KEY_OBJECT = "user";
    private static final String SESSIONS = "sessions";
    private static final String USERS = "users";

    private String sessionId = null;
    private String username = null;
    private AppstaxObject object = null;

    protected static AppstaxUser signup(String username, String password) {
        return AppstaxUser.request(AppstaxPaths.users(), username, password);
    }

    protected static AppstaxUser login(String username, String password) {
        return AppstaxUser.request(AppstaxPaths.sessions(), username, password);
    }

    protected static AppstaxUser request(String path, String username, String password) {
        JSONObject req = new JSONObject();
        req.put(KEY_USERNAME, username);
        req.put(KEY_PASSWORD, password);

        JSONObject res = AppstaxClient.request(AppstaxClient.Method.POST, path, req);
        String session = res.getString(KEY_SESSIONID);
        AppstaxObject object = new AppstaxObject(USERS, res.getJSONObject(KEY_OBJECT));
        return new AppstaxUser(username, session, object);
    }

    protected AppstaxUser(String username, String session, AppstaxObject object) {
        this.username = username;
        this.sessionId = session;
        this.object = object;
    }

    protected AppstaxUser logout() {
        AppstaxClient.request(AppstaxClient.Method.DELETE, AppstaxPaths.session(this.sessionId));
        return this;
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
