package com.appstax;

import java.util.List;
import java.util.Map;

public class Ax {

    private static String appKey = "";
    private static String apiUrl = "https://appstax.com/api/latest/";
    private static volatile AxUser currentUser = null;

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

    public static AxObject save(AxObject object) {
        return object.save();
    }

    public static AxObject saveAll(AxObject object) {
        return object.saveAll();
    }

    public static AxObject remove(AxObject object) {
        return object.remove();
    }

    public static AxObject refresh(AxObject object) {
        return object.refresh();
    }

    public static AxFile load(AxFile file) {
        return file.load();
    }

    public static AxObject find(String collection, String id) {
        return AxQuery.find(collection, id);
    }

    public static List<AxObject> find(String collection) {
        return AxQuery.find(collection);
    }

    public static List<AxObject> filter(String collection, String filter) {
        return AxQuery.filter(collection, filter);
    }

    public static List<AxObject> filter(String collection, Map<String, String> properties) {
        return AxQuery.filter(collection, properties);
    }

    public static AxUser getCurrentUser() {
        return Ax.currentUser;
    }

    public static AxUser signup(String username, String password) {
        Ax.currentUser = AxSession.signup(username, password);
        return Ax.currentUser;
    }

    public static AxUser login(String username, String password) {
        Ax.currentUser = AxSession.login(username, password);
        return Ax.currentUser;
    }

    public static void logout() {
        if (Ax.currentUser != null) {
            AxSession.logout(Ax.currentUser);
            Ax.currentUser = null;
        }
    }

    public static AxUser save(AxUser user) {
        user.getObject().save();
        return user;
    }

    public static AxUser refresh(AxUser user) {
        user.getObject().refresh();
        return user;
    }

}
