package com.appstax;

import org.json.JSONObject;

public final class AppstaxObject {

    private final String KEY_CREATED = "sysCreated";
    private final String KEY_UPDATED = "sysUpdated";
    private final String KEY_ID = "sysObjectId";

    private String collection;
    private JSONObject properties;

    public static AppstaxObject find(String collection, String id) {
        String path = AppstaxPaths.object(collection, id);
        JSONObject properties = AppstaxClient.request(AppstaxClient.Method.GET, path);
        return new AppstaxObject(collection, properties);
    }

    public AppstaxObject(String collection) {
        this(collection, new JSONObject());
    }

    public AppstaxObject(String collection, JSONObject properties) {
        this.collection = collection;
        this.properties = properties;
    }

    public String getId() {
        return this.properties.has(KEY_ID) ? this.properties.getString(KEY_ID) : null;
    }

    public String getCollection() {
        return this.collection;
    }

    public void put(String key, Object val) {
        this.properties.put(key, val);
    }

    public Object get(String key) {
        return this.properties.has(key) ? this.properties.get(key) : null;
    }

    public void save() {
        if (this.getId() == null) {
            post();
        } else {
            put();
        }
    }

    private void post() {
        String path = AppstaxPaths.collection(this.collection);
        JSONObject meta = AppstaxClient.request(AppstaxClient.Method.POST, path, this.properties);
        this.put(KEY_CREATED, meta.get(KEY_CREATED));
        this.put(KEY_UPDATED, meta.get(KEY_UPDATED));
        this.put(KEY_ID, meta.get(KEY_ID));
    }

    private void put() {
        String path = AppstaxPaths.object(this.collection, this.getId());
        JSONObject meta = AppstaxClient.request(AppstaxClient.Method.PUT, path, this.properties);
        this.put(KEY_UPDATED, meta.get(KEY_UPDATED));
    }

    public void delete() {
        String path = AppstaxPaths.collection(this.collection);
        JSONObject meta = AppstaxClient.request(AppstaxClient.Method.DELETE, path, this.properties);
    }

}
