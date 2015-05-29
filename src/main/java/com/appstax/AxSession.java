package com.appstax;

import org.json.JSONObject;

final class AxSession {

    private static final String KEY_SESSIONID = "sysSessionId";
    private static final String KEY_USERNAME = "sysUsername";
    private static final String KEY_PASSWORD = "sysPassword";
    private static final String KEY_OBJECT = "user";
    private static final String USERS = "users";

    protected static AxUser signup(String username, String password) {
        return request(AxPaths.users(), username, password);
    }

    protected static AxUser login(String username, String password) {
        return request(AxPaths.sessions(), username, password);
    }

    protected static AxUser logout(AxUser user) {
        AxClient.request(AxClient.Method.DELETE, AxPaths.session(user.getSessionId()));
        return user;
    }

    private static AxUser request(String path, String username, String password) {
        JSONObject req = new JSONObject();
        req.put(KEY_USERNAME, username);
        req.put(KEY_PASSWORD, password);

        JSONObject res = AxClient.request(AxClient.Method.POST, path, req);
        String session = res.getString(KEY_SESSIONID);
        AxObject object = new AxObject(USERS, res.getJSONObject(KEY_OBJECT));
        return new AxUser(username, session, object);
    }

}
