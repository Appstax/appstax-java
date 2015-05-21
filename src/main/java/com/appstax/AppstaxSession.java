package com.appstax;

import org.json.JSONObject;

abstract class AppstaxSession {

    private static final String KEY_SESSIONID = "sysSessionId";
    private static final String KEY_USERNAME = "sysUsername";
    private static final String KEY_PASSWORD = "sysPassword";
    private static final String KEY_OBJECT = "user";
    private static final String USERS = "users";

    protected static AppstaxUser signup(String username, String password) {
        return request(AppstaxPaths.users(), username, password);
    }

    protected static AppstaxUser login(String username, String password) {
        return request(AppstaxPaths.sessions(), username, password);
    }

    protected static AppstaxUser logout(AppstaxUser user) {
        AppstaxClient.request(AppstaxClient.Method.DELETE, AppstaxPaths.session(user.getSessionId()));
        return user;
    }

    private static AppstaxUser request(String path, String username, String password) {
        JSONObject req = new JSONObject();
        req.put(KEY_USERNAME, username);
        req.put(KEY_PASSWORD, password);

        JSONObject res = AppstaxClient.request(AppstaxClient.Method.POST, path, req);
        String session = res.getString(KEY_SESSIONID);
        AppstaxObject object = new AppstaxObject(USERS, res.getJSONObject(KEY_OBJECT));
        return new AppstaxUser(username, session, object);
    }

}
