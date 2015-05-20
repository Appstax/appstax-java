package com.appstax;

import java.util.List;

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

    public static List<AppstaxObject> find(String collection) {
        return AppstaxObject.find(collection);
    }

    public static AppstaxObject find(String collection, String id) {
        return AppstaxObject.find(collection, id);
    }

    public static AppstaxObject save(AppstaxObject object) {
        return object.save();
    }

    public static AppstaxObject delete(AppstaxObject object) {
        return object.delete();
    }

}
