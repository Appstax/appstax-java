package com.appstax;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class AxQuery {

    private static final String KEY_OBJECTS = "objects";
    private static final String OPERATOR = " and ";

    protected static AxObject find(String collection, String id) {
        String path = AxPaths.object(collection, id);
        JSONObject properties = AxClient.request(AxClient.Method.GET, path);
        return new AxObject(collection, properties);
    }

    protected static List<AxObject> find(String collection) {
        String path = AxPaths.collection(collection);
        return objects(collection, AxClient.request(AxClient.Method.GET, path));
    }

    protected static List<AxObject> filter(String collection, String filter) {
        String path = AxPaths.filter(collection, filter);
        return objects(collection, AxClient.request(AxClient.Method.GET, path));
    }

    protected static List<AxObject> filter(String collection, Map<String, String> properties) {
        StringBuilder builder = new StringBuilder();

        for (Map.Entry<String, String> entry : properties.entrySet()) {
            builder.append(OPERATOR + entry.getKey() + "='" + entry.getValue() + "'");
        }

        return filter(collection, builder.toString().replaceFirst(OPERATOR, ""));
    }

    private static List<AxObject> objects(String collection, JSONObject json) {
        ArrayList<AxObject> objects = new ArrayList<AxObject>();
        JSONArray array = json.getJSONArray(KEY_OBJECTS);

        for(int i = 0; i < array.length(); i++) {
            objects.add(new AxObject(collection, array.getJSONObject(i)));
        }

        return objects;
    }

}
