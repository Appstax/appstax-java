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

    protected void requestPasswordReset(String email) {
        JSONObject data = new JSONObject();
        data.put("email", email);
        client.request(AxClient.Method.POST, AxPaths.requestPasswordReset(), data);
    }

    protected AxUser changePassword(String username, String password, String code, boolean login) {
        JSONObject data = new JSONObject();
        data.put("username", username);
        data.put("password", password);
        data.put("pinCode", code);
        data.put("login", login);

        JSONObject res = client.request(AxClient.Method.POST, AxPaths.changePassword(), data);
        AxUser user = null;
        if(login) {
            user = userFromResponse(res);
        }
        return user;
    }

    public AxAuthConfig getAuthConfig(String provider) {
        JSONObject res = client.request(AxClient.Method.GET, AxPaths.authConfig(provider));
        return AxAuthConfig.config(provider, res.getString("clientId"));
    }

    public AxUser loginWithProvider(String provider, AxAuthResult authResult) {
        JSONObject providerData = new JSONObject()
                .put("code", authResult.getAuthCode())
                .put("redirectUri", authResult.getRedirectUri());
        JSONObject sysProvider = new JSONObject()
                .put("type", provider)
                .put("data", providerData);
        JSONObject data = new JSONObject()
                .put("sysProvider", sysProvider);
        JSONObject res = client.request(AxClient.Method.POST, AxPaths.sessions(), data);
        return userFromResponse(res);
    }

    private AxUser request(String path, String username, String password) {
        JSONObject req = new JSONObject();
        req.put("sysUsername", username);
        req.put("sysPassword", password);

        JSONObject res = client.request(AxClient.Method.POST, path, req);
        return new AxUser(client, username, res.getString("sysSessionId"), res.getJSONObject("user"));
    }

    private AxUser userFromResponse(JSONObject res) {
        return new AxUser(
                client,
                res.getJSONObject("user").getString("sysUsername"),
                res.getString("sysSessionId"),
                res.getJSONObject("user")
        );
    }
}
