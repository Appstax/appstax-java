package com.appstax;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

abstract class AppstaxPaths {

    protected static String users() {
        return "users";
    }

    protected static String sessions() {
        return "sessions";
    }

    protected static String session(String sessionId) {
        return "sessions/" + sessionId;
    }

    protected static String collection(String collection) {
        return "objects/" + collection;
    }

    protected static String object(String collection, String id) {
        return AppstaxPaths.collection(collection) + "/" + id;
    }

    protected static String permissions() {
        return "permissions";
    }

    protected static String file(String collection, String id, String key, String filename) {
        return "files/" + collection + "/" + id + "/" + key + "/" + filename;
    }

    protected static String filter(String collection, String filter) {
        try {
            return AppstaxPaths.collection(collection) + "?filter=" + URLEncoder.encode(filter, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new AppstaxException(e.getMessage(), e);
        }
    }

}
