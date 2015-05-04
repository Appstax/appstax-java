package com.appstax;

import com.appstax.network.AppstaxClient;
import com.appstax.network.AppstaxResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class AppstaxObject {

    private String id;
    private String collection;
    private Map<String, Object> properties;

    public static AppstaxObject find(String id) {
        return null;
    }

    public AppstaxObject(String collection) {
        this.collection = collection;
        this.properties = new ConcurrentHashMap<String, Object>();
    }

    public String getId() {
        return this.id;
    }

    private void setId(String id) {
        this.id = id;
    }

    public String getCollection() {
        return this.collection;
    }

    public void put(String key, Object val) {
        this.properties.put(key, val);
    }

    public Object get(String key) {
        return this.properties.get(key);
    }

    public void save() {
        AppstaxResponse response = AppstaxClient.request(AppstaxClient.Method.POST, this.path(), this.properties);
        this.setId(response.sysObjectId);
    }

    private String path() {
        return "objects/" + this.collection;
    }

}
