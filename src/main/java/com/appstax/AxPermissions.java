package com.appstax;

import org.json.JSONArray;
import org.json.JSONObject;

final class AxPermissions {

    private static final String KEY_GRANTS = "grants";
    private static final String KEY_REVOKES = "revokes";
    private static final String KEY_PERMISSIONS = "permissions";
    private static final String KEY_USER = "username";

    private AxClient client;
    private JSONObject access;

    protected AxPermissions(AxClient client) {
        this.client = client;
        this.access = new JSONObject();
        this.access.put(KEY_GRANTS, new JSONArray());
        this.access.put(KEY_REVOKES, new JSONArray());
    }

    protected void grantPublic(String id, String... permissions) {
        this.create(id, KEY_GRANTS, "*", permissions);
    }

    protected void grant(String id, String username, String... permissions) {
        this.create(id, KEY_GRANTS, username, permissions);
    }

    protected void revokePublic(String id, String... permissions) {
        this.create(id, KEY_REVOKES, "*", permissions);
    }

    protected void revoke(String id, String username, String... permissions) {
        this.create(id, KEY_REVOKES, username, permissions);
    }

    protected void save() {
        if (updates()) {
            String path = AxPaths.permissions();
            client.request(AxClient.Method.POST, path, this.access);
        }
    }

    private boolean updates() {
        return (
            this.access.getJSONArray(KEY_GRANTS).length() > 0 ||
            this.access.getJSONArray(KEY_REVOKES).length() > 0
        );
    }

    private void create(String id, String type, String username, String[] items) {
        if (items.length > 0) {
            JSONObject grant = new JSONObject();
            grant.put(AxObject.KEY_ID, id);
            grant.put(KEY_USER, username);
            grant.put(KEY_PERMISSIONS, new JSONArray(items));
            this.access.getJSONArray(type).put(grant);
        }
    }

}
