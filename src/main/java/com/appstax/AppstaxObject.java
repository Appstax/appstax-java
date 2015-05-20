package com.appstax;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class AppstaxObject {

    private static final String KEY_OBJECTS = "objects";
    private static final String KEY_CREATED = "sysCreated";
    private static final String KEY_UPDATED = "sysUpdated";
    private static final String KEY_ID = "sysObjectId";

    private String collection;
    private JSONObject properties;

    public AppstaxObject(String collection) {
        this(collection, new JSONObject());
    }

    public AppstaxObject(String collection, JSONObject properties) {
        this.collection = collection;
        this.properties = properties;
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

    protected AppstaxObject save() {
        return this.getId() == null ?
                this.post() :
                this.put();
    }

    protected AppstaxObject post() {
        String path = AppstaxPaths.collection(this.getCollection());
        JSONObject meta = AppstaxClient.request(AppstaxClient.Method.POST, path, this.properties);
        this.put(KEY_CREATED, meta.get(KEY_CREATED));
        this.put(KEY_UPDATED, meta.get(KEY_UPDATED));
        this.put(KEY_ID, meta.get(KEY_ID));
        return this;
    }

    protected AppstaxObject put() {
        String path = AppstaxPaths.object(this.getCollection(), this.getId());
        JSONObject meta = AppstaxClient.request(AppstaxClient.Method.PUT, path, this.properties);
        this.put(KEY_UPDATED, meta.get(KEY_UPDATED));
        return this;
    }

    protected AppstaxObject delete() {
        String path = AppstaxPaths.object(this.getCollection(), this.getId());
        this.properties = AppstaxClient.request(AppstaxClient.Method.DELETE, path, this.properties);
        return this;
    }

    protected static List<AppstaxObject> find(String collection) {
        String path = AppstaxPaths.collection(collection);
        ArrayList<AppstaxObject> objects = new ArrayList<AppstaxObject>();

        JSONObject json = AppstaxClient.request(AppstaxClient.Method.GET, path);
        JSONArray array = json.getJSONArray(KEY_OBJECTS);

        for(int i = 0; i < array.length(); i++) {
            objects.add(new AppstaxObject(collection, array.getJSONObject(i)));
        }

        return objects;
    }

    protected static AppstaxObject find(String collection, String id) {
        String path = AppstaxPaths.object(collection, id);
        JSONObject properties = AppstaxClient.request(AppstaxClient.Method.GET, path);
        return new AppstaxObject(collection, properties);
    }

}
