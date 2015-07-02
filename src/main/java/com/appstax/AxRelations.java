package com.appstax;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

final class AxRelations {

    private AxClient client;
    private Map<String, List<AxObject>> relations;
    private Map<String, List<AxObject>> additions;
    private Map<String, List<AxObject>> removals;

    protected AxRelations(AxClient client, JSONObject properties) {
        this.client = client;
        this.relations = parse(properties);
        this.additions = new HashMap<>();
        this.removals = new HashMap<>();
    }

    protected List<AxObject> all(String relation) {
        if (this.relations == null) {
            return null;
        } else if (this.relations.containsKey(relation)) {
            return this.relations.get(relation);
        } else {
            return null;
        }
    }

    protected AxObject one(String relation) {
        if (this.relations == null) {
            return null;
        } else if (this.relations.containsKey(relation)) {
            return this.relations.get(relation).get(0);
        } else {
            return null;
        }
    }

    protected void createRelation(String relation, AxObject... additions) {
        List<AxObject> objects = verify(additions);
        add(relation, this.relations, objects);
        add(relation, this.additions, objects);
    }

    protected void removeRelation(String relation, AxObject... removals) {
        List<AxObject> objects = verify(removals);
        remove(relation, this.relations, objects);
        add(relation, this.removals, objects);
    }

    protected void appendChanges(AxObject object) {
        if (additions.isEmpty() && removals.isEmpty()) {
            return;
        }
        for (String key : keys()) {
            object.put(key, toJSON(
                    additions.get(key),
                    removals.get(key)
            ));
        }
    }

    protected void removeChanges(AxObject object) {
        for (String key : keys()) {
            JSONObject relation = (JSONObject) object.get(key);
            relation.remove("sysRelationChanges");
        }
        this.additions = new HashMap<>();
        this.removals = new HashMap<>();
    }

    protected Set<String> keys() {
        Set<String> keys = new HashSet<>();
        keys.addAll(additions.keySet());
        keys.addAll(removals.keySet());
        return keys;
    }

    protected void flatten(Set<AxObject> objects) {
        for (List<AxObject> list : relations.values()) {
            for (AxObject object : list) {
                if (!objects.contains(object)) {
                    object.flatten(objects);
                }
            }
        }
    }

    private List<AxObject> verify(AxObject... objects) {
        for (AxObject object : objects) {
            if (object.getId() == null) {
                throw new AxException("Can not relate to an unsaved object.");
            }
        }
        return Arrays.asList(objects);
    }

    private void add(String relation, Map<String, List<AxObject>> relations, List<AxObject> objects) {
        if (relations.containsKey(relation)) {
            relations.get(relation).addAll(objects);
        } else {
            relations.put(relation, new ArrayList<AxObject>(objects));
        }
    }

    private void remove(String relation, Map<String, List<AxObject>> relations, List<AxObject> objects) {
        if (!relations.containsKey(relation)) {
            throw new AxException("Unknown relation: " + relation);
        }
        for (AxObject object : objects) {
            for (AxObject existing : relations.get(relation)) {
                if (existing.getId().equals(object.getId())) {
                    relations.get(relation).remove(existing);
                }
            }
        }
    }

    private JSONObject toJSON(List<AxObject> additions, List<AxObject> removals) {
        JSONObject changes = new JSONObject();

        if (additions != null && !additions.isEmpty()) {
            changes.put("additions", toJSONArray(additions));
        }

        if (removals != null && !removals.isEmpty()) {
            changes.put("removals", toJSONArray(removals));
        }

        JSONObject value = new JSONObject();
        value.put("sysRelationChanges", changes);
        return value;
    }

    private JSONArray toJSONArray(List<AxObject> objects) {
        JSONArray changes = new JSONArray();

        for (AxObject object : objects) {
            changes.put(object.getId());
        }

        return changes;
    }

    private Map<String, List<AxObject>> parse(JSONObject properties) {
        Map<String, List<AxObject>> relations = new HashMap<>();
        JSONArray names = properties.names();
        List<String> keys = new ArrayList<>();

        if (names == null || names.length() == 0) {
            return relations;
        }

        for(int i = 0; i < names.length(); i++) {
            keys.add(names.getString(i));
        }

        for (String key : keys) {
            JSONObject property = parseRelation(properties, key);

            if (property == null) {
                continue;
            }

            JSONArray items = property.getJSONArray("sysObjects");
            String collection = property.getString("sysCollection");
            properties.remove(key);

            if (items.get(0) instanceof JSONObject) {
                List<AxObject> objects = parseObjects(collection, items);
                properties.put(key, collectIds(objects));
                relations.put(key, objects);
            } else {
                properties.put(key, items);
            }
        }

        return relations;
    }

    private JSONObject parseRelation(JSONObject properties, String key) {
        if (!(properties.get(key) instanceof JSONObject)) {
            return null;
        }

        JSONObject prop = (JSONObject) properties.get(key);

        if (!prop.has("sysDatatype") || !prop.getString("sysDatatype").equals("relation")) {
            return null;
        }

        return prop;
    }

    private List<AxObject> parseObjects(String collection, JSONArray items) {
        List<AxObject> objects = new ArrayList<>();

        for (int i = 0; i < items.length(); i++) {
            JSONObject data = items.getJSONObject(i);
            objects.add(new AxObject(client, collection, data));
        }

        return objects;
    }

    private JSONArray collectIds(List<AxObject> objects) {
        JSONArray ids = new JSONArray();

        for (AxObject object : objects) {
            ids.put(object.getId());
        }

        return ids;
    }

}
