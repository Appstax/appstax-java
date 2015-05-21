package com.appstax;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

abstract class AppstaxQuery {

    private static final String KEY_OBJECTS = "objects";
    private static final String OPERATOR = " and ";

    protected static AppstaxObject find(String collection, String id) {
        String path = AppstaxPaths.object(collection, id);
        JSONObject properties = AppstaxClient.request(AppstaxClient.Method.GET, path);
        return new AppstaxObject(collection, properties);
    }

    protected static List<AppstaxObject> find(String collection) {
        String path = AppstaxPaths.collection(collection);
        return objects(collection, AppstaxClient.request(AppstaxClient.Method.GET, path));
    }

    protected static List<AppstaxObject> filter(String collection, String filter) {
        String path = AppstaxPaths.filter(collection, filter);
        return objects(collection, AppstaxClient.request(AppstaxClient.Method.GET, path));
    }

    protected static List<AppstaxObject> filter(String collection, Map<String, String> properties) {
        StringBuilder builder = new StringBuilder();

        for (Map.Entry<String, String> entry : properties.entrySet()) {
            builder.append(OPERATOR + entry.getKey() + "='" + entry.getValue() + "'");
        }

        return filter(collection, builder.toString().replaceFirst(OPERATOR, ""));
    }

    private static List<AppstaxObject> objects(String collection, JSONObject json) {
        ArrayList<AppstaxObject> objects = new ArrayList<AppstaxObject>();
        JSONArray array = json.getJSONArray(KEY_OBJECTS);

        for(int i = 0; i < array.length(); i++) {
            objects.add(new AppstaxObject(collection, array.getJSONObject(i)));
        }

        return objects;
    }

}
