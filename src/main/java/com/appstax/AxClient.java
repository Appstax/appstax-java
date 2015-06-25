package com.appstax;

import com.squareup.okhttp.*;
import com.squareup.okhttp.ws.WebSocketCall;
import com.squareup.okhttp.ws.WebSocketListener;
import okio.BufferedSink;
import org.json.JSONObject;

import java.io.IOException;

final class AxClient {

    protected static enum Method {
        GET,
        PUT,
        POST,
        DELETE
    }

    private static OkHttpClient client = new OkHttpClient();

    protected static JSONObject request(Method method, String path) {
        return request(method, path, null);
    }

    protected static JSONObject request(Method method, String path, JSONObject json) {
        Request.Builder req = new Request.Builder();
        setPath(req, path);
        setKeys(req);

        String type = "application/json; charset=utf-8";
        req.addHeader("Content-Type", type);
        req.addHeader("Accept", type);

        String content = json != null ? json.toString() : "";
        RequestBody body = RequestBody.create(MediaType.parse(type), content);
        setBody(req, method, body);

        return parse(execute(req.build()));
    }

    protected static JSONObject multipart(Method method, String path, String field, String name, final byte[] data) {
        Request.Builder req = new Request.Builder();
        req.addHeader("Content-Type", "multipart/form-data");
        setPath(req, path);
        setKeys(req);

        MultipartBuilder multipart = new MultipartBuilder();
        multipart.addFormDataPart(field, name, new RequestBody() {
            public MediaType contentType() {
                return MediaType.parse("application/octet-stream");
            }
            public void writeTo(BufferedSink bufferedSink) throws IOException {
                bufferedSink.write(data);
            }
        });

        RequestBody body = multipart.build();
        setBody(req, method, body);
        return parse(execute(req.build()));
    }

    protected static byte[] file(Method method, String path) {
        try {
            Request req = emptyRequest(method, path);
            Response res = client.newCall(req).execute();
            checkReturnCode(res);
            return res.body().bytes();
        } catch (IOException e) {
            throw new AxException(e.getMessage(), e);
        }
    }

    protected static void socket(String path, WebSocketListener listener) {
        WebSocketCall.create(client, emptyRequest(Method.GET, path)).enqueue(listener);
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

    private static Request emptyRequest(Method method, String path) {
        Request.Builder req = new Request.Builder();
        setPath(req, path);
        setKeys(req);
        setBody(req, method, null);
        return req.build();
    }

    private static void setPath(Request.Builder req, String path) {
        req.url(path);
    }

    private static void setKeys(Request.Builder req) {
        setAppKey(req);
        setSessionKey(req);
    }

    private static void setAppKey(Request.Builder req) {
        if (Ax.getAppKey().equals("")) {
            throw new AxException("Set the app key before making requests.");
        } else {
            req.addHeader("x-appstax-appkey", Ax.getAppKey());
        }
    }

    private static void setSessionKey(Request.Builder req) {
        if (Ax.getCurrentUser() != null) {
            req.addHeader("x-appstax-sessionid", Ax.getCurrentUser().getSessionId());
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
                json.getString("errorId"),
                json.getString("errorCode"),
                json.getString("errorMessage")
            );
        } catch (IOException e) {
            throw new AxException(e.getMessage(), e);
        }
    }

    private static JSONObject parse(String body) {
        if (body == null || body.length() == 0) {
            return new JSONObject();
        }
        if (body.startsWith("{")) {
            return new JSONObject(body);
        }
        throw new AxException(body);
    }

}
