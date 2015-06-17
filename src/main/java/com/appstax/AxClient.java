package com.appstax;

import com.squareup.okhttp.*;
import okio.BufferedSink;
import org.json.JSONObject;

import java.io.IOException;

final class AxClient {

    private static final String ERROR_ID = "errorId";
    private static final String ERROR_CODE = "errorCode";
    private static final String ERROR_MESSAGE = "errorMessage";
    private static final String ERROR_KEY = "Set the app key before making requests.";

    private static final String HEADER_APP_KEY = "x-appstax-appkey";
    private static final String HEADER_SESSION_ID = "x-appstax-sessionid";
    private static final String HEADER_TYPE_JSON = "application/json; charset=utf-8";
    private static final String HEADER_TYPE_STREAM = "application/octet-stream";
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
        return parse(execute(jsonRequest(method, path, json)));
    }

    protected static JSONObject multipart(Method method, String path, String field, String name, byte[] data) {
        return parse(execute(multipartRequest(method, path, field, name, data)));
    }

    protected static byte[] file(Method method, String path) {
        try {
            Request req = fileRequest(method, path);
            Response res = client.newCall(req).execute();
            checkReturnCode(res);
            return res.body().bytes();
        } catch (IOException e) {
            throw new AxException(e.getMessage(), e);
        }
    }

    private static String execute(Request req) {
        try {
            Response res = client.newCall(req).execute();
            checkReturnCode(res);
            return res.body().string();
        } catch (IOException e) {
            throw new AxException(e.getMessage(), e);
        }
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

    private static Request multipartRequest(Method method, String path, String field, String name, final byte[] data) {
        Request.Builder req = new Request.Builder();
        req.addHeader(HEADER_CONTENT_TYPE, HEADER_TYPE_FORM);
        setPath(req, path);
        setKeys(req);

        MultipartBuilder multipart = new MultipartBuilder();
        multipart.addFormDataPart(field, name, new RequestBody() {
            public MediaType contentType() {
                return MediaType.parse(HEADER_TYPE_STREAM);
            }
            public void writeTo(BufferedSink bufferedSink) throws IOException {
                bufferedSink.write(data);
            }
        });

        RequestBody body = multipart.build();
        setBody(req, method, body);
        return req.build();
    }

    private static Request fileRequest(Method method, String path) {
        Request.Builder req = new Request.Builder();
        setPath(req, path);
        setKeys(req);
        return req.build();
    }

    private static void setPath(Request.Builder req, String path) {
        req.url(Ax.getApiUrl() + path);
    }

    private static void setKeys(Request.Builder req) {
        setAppKey(req);
        setSessionKey(req);
    }

    private static void setAppKey(Request.Builder req) {
        if (Ax.getAppKey().equals("")) {
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

    private static void setBody(Request.Builder req, Method method, RequestBody body) {
        if (method == Method.GET) req = req.get();
        if (method == Method.PUT) req = req.put(body);
        if (method == Method.POST) req = req.post(body);
        if (method == Method.DELETE) req = req.delete(body);
    }

    private static void checkReturnCode(Response res) {
        if (res.isSuccessful()) {
            return;
        }
        try {
            JSONObject json = parse(res.body().string());
            throw new AxException(
                res.code(),
                json.getString(ERROR_ID),
                json.getString(ERROR_CODE),
                json.getString(ERROR_MESSAGE)
            );
        } catch (IOException e) {
            throw new AxException(e.getMessage(), e);
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
