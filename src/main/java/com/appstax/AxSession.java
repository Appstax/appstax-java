package com.appstax;

import org.json.JSONObject;

final class AxSession {

    private AxClient client;

    protected AxSession(AxClient client) {
        this.client = client;
    }

    protected AxUser signup(String username, String password) {
        return request(AxPaths.users(), username, password);
    }

    protected AxUser login(String username, String password) {
        return request(AxPaths.sessions(), username, password);
    }

    protected AxUser logout(AxUser user) {
        client.request(AxClient.Method.DELETE, AxPaths.session(user.getSessionId()));
        return user;
    }

    private AxUser request(String path, String username, String password) {
        JSONObject req = new JSONObject();
        req.put("sysUsername", username);
        req.put("sysPassword", password);

        JSONObject res = client.request(AxClient.Method.POST, path, req);
        return new AxUser(client, username, res.getString("sysSessionId"), res.getJSONObject("user"));
    }

}
