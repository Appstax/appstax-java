package com.appstax;

import com.squareup.okhttp.*;
import com.squareup.okhttp.ws.WebSocketCall;
import com.squareup.okhttp.ws.WebSocketListener;
import okio.BufferedSink;
import org.json.JSONObject;

import java.io.IOException;

final class AxClient {

    protected enum Method { GET, PUT, POST, DELETE }

    private static String TYPE_JSON = "application/json; charset=utf-8";
    private static String TYPE_FORM = "multipart/form-data";
    private static String TYPE_BIN = "application/octet-stream";

    private String key;
    private String url;
    private AxUser user;
    private OkHttpClient ok = new OkHttpClient();

    protected AxClient(String key, String url) {
        this.key = key;
        this.url = url;
    }

    protected AxUser getUser() {
        return user;
    }

    protected void setUser(AxUser user) {
        this.user = user;
    }

    public String getUrl() {
        return url;
    }

    public String getSocketUrl() {
        return url.replaceFirst("^http", "ws");
    }

    protected JSONObject request(Method method, String path) {
        return request(method, path, null);
    }

    protected JSONObject request(Method method, String path, JSONObject json) {
        Request.Builder req = new Request.Builder();
        setHttpPath(req, path);
        setKeys(req);

        req.addHeader("Content-Type", TYPE_JSON);
        req.addHeader("Accept", TYPE_JSON);

        String content = json != null ? json.toString() : "";
        RequestBody body = RequestBody.create(MediaType.parse(TYPE_JSON), content);
        setBody(req, method, body);

        return parse(execute(req.build()));
    }

    protected JSONObject multipart(Method method, String path, String field, String name, final byte[] data) {
        Request.Builder req = new Request.Builder();
        req.addHeader("Content-Type", TYPE_FORM);
        setHttpPath(req, path);
        setKeys(req);

        MultipartBuilder multipart = new MultipartBuilder();
        multipart.addFormDataPart(field, name, new RequestBody() {
            public MediaType contentType() {
                return MediaType.parse(TYPE_BIN);
            }
            public void writeTo(BufferedSink bufferedSink) throws IOException {
                bufferedSink.write(data);
            }
        });

        RequestBody body = multipart.build();
        setBody(req, method, body);
        return parse(execute(req.build()));
    }

    protected byte[] file(Method method, String path) {
        try {
            Request.Builder req = new Request.Builder();
            setHttpPath(req, path);
            setKeys(req);
            setBody(req, method, null);
            Response res = ok.newCall(req.build()).execute();
            checkReturnCode(res);
            return res.body().bytes();
        } catch (IOException e) {
            throw new AxException(e.getMessage(), e);
        }
    }

    protected void socket(String path, WebSocketListener listener) {
        Request.Builder req = new Request.Builder();
        setWsPath(req, path);
        setKeys(req);
        setBody(req, Method.GET, null);
        WebSocketCall.create(ok, req.build()).enqueue(listener);
    }

    private String execute(Request req) {
        try {
            Response res = ok.newCall(req).execute();
            checkReturnCode(res);
            return res.body().string();
        } catch (IOException e) {
            throw new AxException(e.getMessage(), e);
        }
    }

    private void setHttpPath(Request.Builder req, String path) {
        req.url(path.startsWith("http") ? path : getUrl() + path);
    }

    private void setWsPath(Request.Builder req, String path) {
        req.url(path.startsWith("ws") ? path : getSocketUrl() + path);
    }

    private void setKeys(Request.Builder req) {
        setAppKey(req);
        setSessionKey(req);
    }

    private void setAppKey(Request.Builder req) {
        req.addHeader("x-appstax-appkey", key);
    }

    private void setSessionKey(Request.Builder req) {
        if (getUser() != null) {
            req.addHeader("x-appstax-sessionid", getUser().getSessionId());
        }
    }

    private void setBody(Request.Builder req, Method method, RequestBody body) {
        if (method == Method.GET) req.get();
        if (method == Method.PUT) req.put(body);
        if (method == Method.POST) req.post(body);
        if (method == Method.DELETE) req.delete(body);
    }

    private void checkReturnCode(Response res) {
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

    private JSONObject parse(String body) {
        if (body == null || body.length() == 0) {
            return new JSONObject();
        }
        if (body.startsWith("{")) {
            return new JSONObject(body);
        }
        return new JSONObject();
    }

}
