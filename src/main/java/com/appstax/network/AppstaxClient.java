package com.appstax.network;

import com.appstax.Appstax;
import com.appstax.exceptions.AppstaxRequestException;
import com.squareup.okhttp.*;
import org.json.JSONObject;

import java.io.IOException;

public final class AppstaxClient {

    private static final String HEADER_APP_KEY = "x-appstax-appkey";
    private static final String HEADER_SESSION_ID = "x-appstax-sessionid";
    private static final String HEADER_ACCEPT = "Accept";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String HEADER_TYPE_JSON = "application/json; charset=utf-8";

    private static OkHttpClient client = new OkHttpClient();
    public static enum Method { GET, PUT, POST, DELETE }

    public static JSONObject request(Method method, String path) {
        return request(method, path, null);
    }

    public static JSONObject request(Method method, String path, JSONObject json) {
        try {
            Request req = build(method, path, json);
            Response res = client.newCall(req).execute();
            String body = res.body().string();
            check(res, body);
            return new JSONObject(body);
        } catch (IOException e) {
            throw new AppstaxRequestException("Connection error", e);
        }
    }

    private static Request build(Method method, String path, JSONObject json) {
        Request.Builder req = new Request.Builder()
                .url(Appstax.getApiUrl() + path)
                .addHeader(HEADER_APP_KEY, Appstax.getAppKey())
                .addHeader(HEADER_CONTENT_TYPE, HEADER_TYPE_JSON)
                .addHeader(HEADER_ACCEPT, HEADER_TYPE_JSON);

        String content = json != null ? json.toString() : "";
        MediaType media = MediaType.parse(HEADER_TYPE_JSON);
        RequestBody body = RequestBody.create(media, content);

        if (method == Method.GET) req = req.get();
        if (method == Method.PUT) req = req.put(body);
        if (method == Method.POST) req = req.post(body);
        if (method == Method.DELETE) req = req.delete(body);

        return req.build();
    }

    private static void check(Response response, String body) {
        if (!response.isSuccessful()) {
            throw new AppstaxRequestException(body);
        }
    }

}
