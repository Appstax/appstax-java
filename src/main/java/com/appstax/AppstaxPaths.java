package com.appstax;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

abstract class AppstaxPaths {

    public static String users() {
        return "users";
    }

    public static String sessions() {
        return "sessions";
    }

    public static String session(String sessionId) {
        return "sessions/" + sessionId;
    }

    public static String collection(String collection) {
        return "objects/" + collection;
    }

    public static String object(String collection, String id) {
        return AppstaxPaths.collection(collection) + "/" + id;
    }

    public static String permissions() {
        return "permissions";
    }

    public static String file(String collection, String id, String key, String filename) {
        return "files/" + collection + "/" + id + "/" + key + "/" + filename;
    }

    public static String filter(String collection, String filter) {
        try {
            return AppstaxPaths.collection(collection) + "?filter=" + URLEncoder.encode(filter, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new AppstaxException(e.getMessage(), e);
        }
    }

}
