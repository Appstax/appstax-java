package com.appstax;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

final class AxPaths {

    protected static String users() {
        return url("users");
    }

    protected static String sessions() {
        return url("sessions");
    }

    protected static String session(String sessionId) {
        return url("sessions/" + sessionId);
    }

    protected static String collection(String col) {
        return url("objects/" + col);
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
        return url("permissions");
    }

    protected static String file(String col, String id, String key, String filename) {
        return url("files/" + col + "/" + id + "/" + key + "/" + filename);
    }

    protected static String filter(String col, String filter) {
        return collection(col) + "?filter=" + encode(filter);
    }

    protected static String realtimeSessions() {
        return url("messaging/realtime/sessions");
    }

    protected static String realtime(String realtimeSessionId) {
        return socket("messaging/realtime?rsession=" + realtimeSessionId);
    }

    private static String expand(String path, int depth) {
        return depth == 0 ? path : path + "?expanddepth=" + depth;
    }

    private static String url(String path) {
        return Ax.getApiUrl() + path;
    }

    private static String socket(String path) {
        return Ax.getApiSocket() + path;
    }

    protected static String encode(String component) {
        try {
            return URLEncoder.encode(component, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new AxException(e);
        }
    }

}
