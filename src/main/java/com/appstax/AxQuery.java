package com.appstax;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class AxQuery {

    private static final String KEY_OBJECTS = "objects";
    private static final String OPERATOR = " and ";

    protected static List<AxObject> find(String collection, int depth) {
        String path = AxPaths.collection(collection, depth);
        return objects(collection, AxClient.request(AxClient.Method.GET, path));
    }

    protected static AxObject find(String collection, String id, int depth) {
        String path = AxPaths.object(collection, id, depth);
        JSONObject properties = AxClient.request(AxClient.Method.GET, path);
        return new AxObject(collection, properties);
    }

    protected static List<AxObject> filter(String collection, String filter) {
        String path = AxPaths.filter(collection, filter);
        return objects(collection, AxClient.request(AxClient.Method.GET, path));
    }

    protected static List<AxObject> filter(String collection, Map<String, String> properties) {
        StringBuilder b = new StringBuilder();

        for (Map.Entry<String, String> entry : properties.entrySet()) {
            b.append(OPERATOR);
            b.append(entry.getKey());
            b.append("='");
            b.append(entry.getValue());
            b.append("'");
        }

        return filter(collection, b.toString().replaceFirst(OPERATOR, ""));
    }

    private static List<AxObject> objects(String collection, JSONObject json) {
        ArrayList<AxObject> objects = new ArrayList<AxObject>();
        JSONArray array = json.getJSONArray(KEY_OBJECTS);

        for(int i = 0; i < array.length(); i++) {
            JSONObject properties = array.getJSONObject(i);
            AxObject object = new AxObject(collection, properties);
            objects.add(object);
        }

        return objects;
    }

}
