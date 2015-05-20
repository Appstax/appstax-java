package com.appstax;

import java.util.List;
import java.util.Map;

public abstract class Appstax {

    private static String appKey = "";
    private static String apiUrl = "https://appstax.com/api/latest/";

    public static String getAppKey() {
        return appKey;
    }

    public static void setAppKey(final String key) {
        appKey = key;
    }

    public static String getApiUrl() {
        return apiUrl;
    }

    public static void setApiUrl(final String url) {
        apiUrl = url.replaceAll("/$", "") + "/";
    }

    public static AppstaxObject save(AppstaxObject object) {
        return object.save();
    }

    public static AppstaxObject remove(AppstaxObject object) {
        return object.remove();
    }

    public static AppstaxObject refresh(AppstaxObject object) {
        return object.refresh();
    }

    public static AppstaxObject find(String collection, String id) {
        return AppstaxObject.find(collection, id);
    }

    public static List<AppstaxObject> find(String collection) {
        return AppstaxObject.find(collection);
    }

    public static List<AppstaxObject> filter(String collection, String filter) {
        return AppstaxObject.filter(collection, filter);
    }

    public static List<AppstaxObject> filter(String collection, Map<String, String> properties) {
        return AppstaxObject.filter(collection, properties);
    }

}
