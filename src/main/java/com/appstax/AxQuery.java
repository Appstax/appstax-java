package com.appstax;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class AxQuery {

    private AxClient client;

    protected AxQuery(AxClient client) {
        this.client = client;
    }

    protected List<AxObject> find(String collection, int depth) {
        String path = AxPaths.collection(collection, depth);
        return objects(collection, client.request(AxClient.Method.GET, path));
    }

    protected AxObject find(String collection, String id, int depth) {
        String path = AxPaths.object(collection, id, depth);
        JSONObject properties = client.request(AxClient.Method.GET, path);
        return new AxObject(client, collection, properties);
    }

    protected List<AxObject> filter(String collection, String filter) {
        String path = AxPaths.filter(collection, filter);
        return objects(collection, client.request(AxClient.Method.GET, path));
    }

    protected List<AxObject> filter(String collection, Map<String, String> properties) {
        StringBuilder b = new StringBuilder();

        for (Map.Entry<String, String> entry : properties.entrySet()) {
            b.append(" and ");
            b.append(entry.getKey());
            b.append("='");
            b.append(entry.getValue());
            b.append("'");
        }

        return filter(collection, b.toString().replaceFirst(" and ", ""));
    }

    private List<AxObject> objects(String collection, JSONObject json) {
        ArrayList<AxObject> objects = new ArrayList<AxObject>();
        JSONArray array = json.getJSONArray("objects");

        for(int i = 0; i < array.length(); i++) {
            objects.add(new AxObject(client, collection, array.getJSONObject(i)));
        }

        return objects;
    }

}
