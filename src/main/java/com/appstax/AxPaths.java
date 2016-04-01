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
        return collection(col) + "/" + id;
    }

    protected static String object(String col, String id, int depth) {
        return expand(object(col, id), depth);
    }

    protected static String permissions() {
        return "permissions";
    }

    protected static String file(String col, String id, String key, String filename) {
        return "files/" + col + "/" + id + "/" + key + "/" + filename;
    }

    protected static String filter(String col, String filter) {
        return collection(col) + "?filter=" + encode(filter);
    }

    protected static String realtimeSessions() {
        return "messaging/realtime/sessions";
    }

    protected static String realtime(String realtimeSessionId) {
        return "messaging/realtime?rsession=" + realtimeSessionId;
    }

    protected static String requestPasswordReset() {
        return "users/reset/email";
    }

    protected static String changePassword() {
        return "users/reset/password";
    }

    private static String expand(String path, int depth) {
        return depth > 0 ? path + "?expanddepth=" + depth : path;
    }

    protected static String encode(String component) {
        try {
            return URLEncoder.encode(component, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new AxException(e);
        }
    }

}
