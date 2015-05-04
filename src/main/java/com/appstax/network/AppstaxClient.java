package com.appstax.network;

import com.appstax.Appstax;
import com.appstax.exceptions.AppstaxException;
import com.appstax.exceptions.AppstaxRequestException;
import com.google.gson.Gson;
import com.squareup.okhttp.*;

import java.io.IOException;
import java.util.Map;

public final class AppstaxClient {

    private static final String HEADER_APP_KEY = "x-appstax-appkey";
    private static final String HEADER_SESSION_ID = "x-appstax-sessionid";
    private static final String HEADER_ACCEPT = "Accept";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String HEADER_TYPE_JSON = "application/json; charset=utf-8";

    public static enum Method {
        GET,
        POST,
        DELETE
    }

    private static OkHttpClient client = new OkHttpClient();

    public static AppstaxResponse request(Method method, String path, Map<String, Object> data) {
        switch (method) {
            case GET: return getRequest(path);
            case POST: return postRequest(path, data);
            default: throw new AppstaxException("Unknown request method");
        }
    }

    public static AppstaxResponse postRequest(String path, Map<String, Object> data) {
        Gson gson = new Gson();
        String json = gson.toJson(data);
        RequestBody body = RequestBody.create(MediaType.parse(HEADER_TYPE_JSON), json);

        Request request = new Request.Builder()
                .url(Appstax.getApiUrl() + path)
                .addHeader(HEADER_APP_KEY, Appstax.getAppKey())
                .addHeader(HEADER_CONTENT_TYPE, HEADER_TYPE_JSON)
                .addHeader(HEADER_ACCEPT, HEADER_TYPE_JSON)
                .post(body)
                .build();

        try {
            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();

            if (!response.isSuccessful()) {
                throw new AppstaxRequestException(responseBody);
            }

            return gson.fromJson(responseBody, AppstaxResponse.class);

        } catch (IOException e) {
            throw new AppstaxRequestException("Connection error", e);
        }

    }

    public static AppstaxResponse getRequest(String path) {
        throw new AppstaxException("Not implemented");
    }

}
