package com.appstax;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class AppstaxObject {

    private static final String KEY_OBJECTS = "objects";
    private static final String KEY_CREATED = "sysCreated";
    private static final String KEY_UPDATED = "sysUpdated";
    private static final String KEY_ID = "sysObjectId";
    private static final String KEY_GRANTS = "grants";
    private static final String KEY_REVOKES = "revokes";
    private static final String KEY_USER = "username";
    private static final String KEY_PERMISSIONS = "permissions";
    private static final String OPERATOR = " and ";

    private String collection;
    private JSONObject properties;
    private JSONObject access;

    public AppstaxObject(String collection) {
        this(collection, new JSONObject());
    }

    public AppstaxObject(String collection, JSONObject properties) {
        this.collection = collection;
        this.properties = properties;
        this.access = new JSONObject();
        this.access.put(KEY_GRANTS, new JSONArray());
        this.access.put(KEY_REVOKES, new JSONArray());
    }

    public String getId() {
        return this.properties.has(KEY_ID) ?
            this.properties.getString(KEY_ID) :
            null;
    }

    public String getCollection() {
        return this.collection;
    }

    public void put(String key, Object val) {
        this.properties.put(key, val);
    }

    public Object get(String key) {
        return this.properties.has(key) ?
            this.properties.get(key) :
            null;
    }

    public AppstaxObject grant(List<String> permissions) {
        return this.grant("*", permissions);
    }

    public AppstaxObject grant(String username, List<String> permissions) {
        return this.permission(KEY_GRANTS, username, permissions);
    }

    public AppstaxObject revoke(List<String> permissions) {
        return this.revoke("*", permissions);
    }

    public AppstaxObject revoke(String username, List<String> permissions) {
        return this.permission(KEY_REVOKES, username, permissions);
    }

    private AppstaxObject permission(String type, String username, List<String> permissions) {
        JSONObject grant = new JSONObject();
        grant.put(KEY_ID, this.getId());
        grant.put(KEY_USER, username);
        grant.put(KEY_PERMISSIONS, new JSONArray(permissions));
        this.access.getJSONArray(type).put(grant);
        return this;
    }

    protected AppstaxObject save() {
        saveObject();
        saveAccess();
        return this;
    }

    private AppstaxObject saveObject() {
        return this.getId() == null ?
            this.post() :
            this.put();
    }

    private AppstaxObject saveAccess() {
        if (this.hasAccess()) {
            String path = AppstaxPaths.permissions();
            AppstaxClient.request(AppstaxClient.Method.POST, path, this.access);
        }
        return this;
    }

    private boolean hasAccess() {
        return (
            this.access.getJSONArray(KEY_GRANTS).length() > 0 ||
            this.access.getJSONArray(KEY_REVOKES).length() > 0
        );
    }

    private AppstaxObject post() {
        String path = AppstaxPaths.collection(this.getCollection());
        JSONObject meta = AppstaxClient.request(AppstaxClient.Method.POST, path, this.properties);
        this.put(KEY_CREATED, meta.get(KEY_CREATED));
        this.put(KEY_UPDATED, meta.get(KEY_UPDATED));
        this.put(KEY_ID, meta.get(KEY_ID));
        return this;
    }

    private AppstaxObject put() {
        String path = AppstaxPaths.object(this.getCollection(), this.getId());
        JSONObject meta = AppstaxClient.request(AppstaxClient.Method.PUT, path, this.properties);
        this.put(KEY_UPDATED, meta.get(KEY_UPDATED));
        return this;
    }

    protected AppstaxObject remove() {
        String path = AppstaxPaths.object(this.getCollection(), this.getId());
        this.properties = AppstaxClient.request(AppstaxClient.Method.DELETE, path);
        return this;
    }

    protected AppstaxObject refresh() {
        String path = AppstaxPaths.object(this.getCollection(), this.getId());
        this.properties = AppstaxClient.request(AppstaxClient.Method.GET, path);
        return this;
    }

    protected static AppstaxObject find(String collection, String id) {
        String path = AppstaxPaths.object(collection, id);
        JSONObject properties = AppstaxClient.request(AppstaxClient.Method.GET, path);
        return new AppstaxObject(collection, properties);
    }

    protected static List<AppstaxObject> find(String collection) {
        String path = AppstaxPaths.collection(collection);
        return objects(collection, AppstaxClient.request(AppstaxClient.Method.GET, path));
    }

    protected static List<AppstaxObject> filter(String collection, String filter) {
        String path = AppstaxPaths.filter(collection, filter);
        return objects(collection, AppstaxClient.request(AppstaxClient.Method.GET, path));
    }

    protected static List<AppstaxObject> filter(String collection, Map<String, String> properties) {
        StringBuilder builder = new StringBuilder();

        for (Map.Entry<String, String> entry : properties.entrySet()) {
            builder.append(OPERATOR + entry.getKey() + "='" + entry.getValue() + "'");
        }

        return filter(collection, builder.toString().replaceFirst(OPERATOR, ""));
    }

    private static List<AppstaxObject> objects(String collection, JSONObject json) {
        ArrayList<AppstaxObject> objects = new ArrayList<AppstaxObject>();
        JSONArray array = json.getJSONArray(KEY_OBJECTS);

        for(int i = 0; i < array.length(); i++) {
            objects.add(new AppstaxObject(collection, array.getJSONObject(i)));
        }

        return objects;
    }

}
