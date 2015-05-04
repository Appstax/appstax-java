package com.appstax;

public abstract class Appstax {

    private static volatile String appKey = "";
    private static volatile String apiUrl = "https://appstax.com/api/latest/";

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

}
