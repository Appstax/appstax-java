package com.appstax;

import org.json.JSONObject;

final class AxSession {

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
        req.put("sysUsername", username);
        req.put("sysPassword", password);

        JSONObject res = AxClient.request(AxClient.Method.POST, path, req);
        return new AxUser(username, res.getString("sysSessionId"), res.getJSONObject("user"));
    }

}
