package com.appstax;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

final class AxPaths {

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
        return AxPaths.collection(collection) + "/" + id;
    }

    protected static String permissions() {
        return "permissions";
    }

    protected static String file(String collection, String id, String key, String filename) {
        return "files/" + collection + "/" + id + "/" + key + "/" + filename;
    }

    protected static String filter(String collection, String filter) {
        try {
            return AxPaths.collection(collection) + "?filter=" + URLEncoder.encode(filter, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new AxException(e.getMessage(), e);
        }
    }

}
