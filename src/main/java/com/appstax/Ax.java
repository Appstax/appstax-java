package com.appstax;

import java.util.*;

public class Ax {

    private static String appKey = "";
    private static String apiUrl = "https://appstax.com/api/latest/";
    private static volatile AxUser currentUser = null;

    public static String getAppKey() {
        return appKey;
    }

    public static void setAppKey(String key) {
        appKey = key;
    }

    public static String getApiUrl() {
        return apiUrl;
    }

    public static void setApiUrl(String url) {
        apiUrl = parseUrl(url);
    }

    public static String getApiSocket() {
        return apiUrl.replaceFirst("^http", "ws");
    }

    public static AxObject save(AxObject object) {
        return object.save();
    }

    public static AxObject saveAll(AxObject object) {
        for (AxObject target : object.flatten(null)) {
            target.save();
        }
        return object;
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

    public static List<AxObject> find(String collection) {
        return AxQuery.find(collection, 0);
    }

    public static List<AxObject> find(String collection, int depth) {
        return AxQuery.find(collection, depth);
    }

    public static AxObject find(String collection, String id) {
        return AxQuery.find(collection, id, 0);
    }

    public static AxObject find(String collection, String id, int depth) {
        return AxQuery.find(collection, id, depth);
    }

    public static List<AxObject> filter(String collection, String filter) {
        return AxQuery.filter(collection, filter);
    }

    public static List<AxObject> filter(String collection, Map<String, String> properties) {
        return AxQuery.filter(collection, properties);
    }

    public static AxChannel channel(String name, AxListener listener) {
        return new AxChannel(name, listener);
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

    private static String parseUrl(String url) {
        return url.replaceAll("/$", "") + "/";
    }

}
