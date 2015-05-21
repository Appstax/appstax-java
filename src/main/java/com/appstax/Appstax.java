package com.appstax;

import java.util.List;
import java.util.Map;

public abstract class Appstax {

    private static String appKey = "";
    private static String apiUrl = "https://appstax.com/api/latest/";
    private static volatile AppstaxUser currentUser = null;

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
        return AppstaxQuery.find(collection, id);
    }

    public static List<AppstaxObject> find(String collection) {
        return AppstaxQuery.find(collection);
    }

    public static List<AppstaxObject> filter(String collection, String filter) {
        return AppstaxQuery.filter(collection, filter);
    }

    public static List<AppstaxObject> filter(String collection, Map<String, String> properties) {
        return AppstaxQuery.filter(collection, properties);
    }

    public static AppstaxUser getCurrentUser() {
        return Appstax.currentUser;
    }

    public static AppstaxUser signup(String username, String password) {
        Appstax.currentUser = AppstaxSession.signup(username, password);
        return Appstax.currentUser;
    }

    public static AppstaxUser login(String username, String password) {
        Appstax.currentUser = AppstaxSession.login(username, password);
        return Appstax.currentUser;
    }

    public static void logout() {
        if (Appstax.currentUser != null) {
            AppstaxSession.logout(Appstax.currentUser);
            Appstax.currentUser = null;
        }
    }

    public static AppstaxUser save(AppstaxUser user) {
        user.getObject().save();
        return user;
    }

    public static AppstaxUser refresh(AppstaxUser user) {
        user.getObject().refresh();
        return user;
    }

}
