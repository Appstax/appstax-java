package com.appstax;

import com.squareup.okhttp.*;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

final class AxClient {

    private static final String ERROR_ID = "errorId";
    private static final String ERROR_CODE = "errorCode";
    private static final String ERROR_MESSAGE = "errorMessage";
    private static final String ERROR_KEY = "Set the app key before making requests.";
    private static final String HEADER_APP_KEY = "x-appstax-appkey";
    private static final String HEADER_SESSION_ID = "x-appstax-sessionid";
    private static final String HEADER_TYPE_JSON = "application/json; charset=utf-8";
    private static final String HEADER_TYPE_FORM = "multipart/form-data";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String HEADER_ACCEPT = "Accept";

    private static OkHttpClient client = new OkHttpClient();

    protected static enum Method {
        GET,
        PUT,
        POST,
        DELETE
    }

    protected static JSONObject request(Method method, String path) {
        return request(method, path, null);
    }

    protected static JSONObject request(Method method, String path, JSONObject json) {
        return execute(jsonRequest(method, path, json));
    }

    protected static JSONObject form(Method method, String path, Map<String, String> form) {
        return execute(formRequest(method, path, form));
    }

    private static JSONObject execute(Request req) {
        try {
            Response res = client.newCall(req).execute();
            JSONObject body = parse(res.body().string());
            checkReturnCode(res, body);
            return body;
        } catch (IOException e) {
            throw new AxException(e.getMessage(), e);
        }
    }

    private static Request formRequest(Method method, String path, Map<String, String> form) {
        Request.Builder req = new Request.Builder();
        setPath(req, path);
        setKeys(req);

        req.addHeader(HEADER_CONTENT_TYPE, HEADER_TYPE_FORM);

        MultipartBuilder multipart = new MultipartBuilder();
        for (Map.Entry<String, String> item : form.entrySet()) {
            multipart.addFormDataPart(item.getKey(), item.getValue());
        }

        RequestBody body = multipart.build();
        setBody(req, method, body);
        return req.build();
    }

    private static Request jsonRequest(Method method, String path, JSONObject json) {
        Request.Builder req = new Request.Builder();
        setPath(req, path);
        setKeys(req);

        req.addHeader(HEADER_CONTENT_TYPE, HEADER_TYPE_JSON);
        req.addHeader(HEADER_ACCEPT, HEADER_TYPE_JSON);

        String content = json != null ? json.toString() : "";
        MediaType media = MediaType.parse(HEADER_TYPE_JSON);
        RequestBody body = RequestBody.create(media, content);
        setBody(req, method, body);
        return req.build();
    }

    private static void setPath(Request.Builder req, String path) {
        req.url(Ax.getApiUrl() + path);
    }

    private static void setBody(Request.Builder req, Method method, RequestBody body) {
        if (method == Method.GET) req = req.get();
        if (method == Method.PUT) req = req.put(body);
        if (method == Method.POST) req = req.post(body);
        if (method == Method.DELETE) req = req.delete(body);
    }

    private static void setKeys(Request.Builder req) {
        setAppKey(req);
        setSessionKey(req);
    }

    private static void setAppKey(Request.Builder req) {
        if (Ax.getAppKey() == "") {
            throw new AxException(ERROR_KEY);
        } else {
            req.addHeader(HEADER_APP_KEY, Ax.getAppKey());
        }
    }

    private static void setSessionKey(Request.Builder req) {
        if (Ax.getCurrentUser() != null) {
            req.addHeader(HEADER_SESSION_ID, Ax.getCurrentUser().getSessionId());
        }
    }

    private static void checkReturnCode(Response response, JSONObject json) {
        if (!response.isSuccessful()) {
            throw new AxException(
                response.code(),
                json.getString(ERROR_ID),
                json.getString(ERROR_CODE),
                json.getString(ERROR_MESSAGE)
            );
        }
    }

    private static JSONObject parse(String body) {
        if (body.length() > 0) {
            return new JSONObject(body);
        } else {
            return new JSONObject();
        }
    }

}
