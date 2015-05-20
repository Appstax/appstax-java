package com.appstax;

import com.squareup.okhttp.*;
import org.json.JSONObject;

import java.io.IOException;

final class AppstaxClient {

    private static final String ERROR_ID = "errorId";
    private static final String ERROR_CODE = "errorCode";
    private static final String ERROR_MESSAGE = "errorMessage";
    private static final String ERROR_KEY = "Use Appstax.setAppKey(\"YourAppKey\") before making requests.";

    private static final String HEADER_APP_KEY = "x-appstax-appkey";
    private static final String HEADER_SESSION_ID = "x-appstax-sessionid";
    private static final String HEADER_TYPE_JSON = "application/json; charset=utf-8";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String HEADER_ACCEPT = "Accept";

    private static OkHttpClient client = new OkHttpClient();
    protected static enum Method { GET, PUT, POST, DELETE }

    protected static JSONObject request(Method method, String path) {
        return request(method, path, null);
    }

    protected static JSONObject request(Method method, String path, JSONObject json) {
        try {
            Request req = build(method, path, json);
            Response res = client.newCall(req).execute();
            JSONObject body = parse(res.body().string());
            check(res, body);
            return body;
        } catch (IOException e) {
            throw new AppstaxException(e.getMessage(), e);
        }
    }

    private static Request build(Method method, String path, JSONObject json) {

        if (Appstax.getAppKey() == "") {
            throw new AppstaxException(ERROR_KEY);
        }

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

    private static void check(Response response, JSONObject json) {
        if (!response.isSuccessful()) {
            throw new AppstaxException(
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
