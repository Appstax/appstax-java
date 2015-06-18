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

    protected static String collection(String col) {
        return "objects/" + col;
    }

    protected static String collection(String col, int depth) {
        return expand(collection(col), depth);
    }

    protected static String object(String col, String id) {
        return AxPaths.collection(col) + "/" + id;
    }

    protected static String object(String col, String id, int depth) {
        return expand(AxPaths.object(col, id), depth);
    }

    protected static String expand(String path, int depth) {
        return depth == 0 ? path : path + "?expanddepth=" + depth;
    }

    protected static String permissions() {
        return "permissions";
    }

    protected static String file(String col, String id, String key, String filename) {
        return "files/" + col + "/" + id + "/" + key + "/" + filename;
    }

    protected static String filter(String col, String filter) {
        try {
            return AxPaths.collection(col) + "?filter=" + URLEncoder.encode(filter, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new AxException(e.getMessage(), e);
        }
    }

}
