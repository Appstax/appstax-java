package com.appstax;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

final class AxRelations {

    private static final String KEY_ADDITIONS = "additions";
    private static final String KEY_REMOVALS = "removals";

    private static final String KEY_COLLECTION = "sysCollection";
    private static final String KEY_OBJECTS = "sysObjects";
    private static final String KEY_RELATIONS = "sysDatatype";

    private static final String VAL_RELATIONS = "relation";
    private static final String KEY_CHANGES = "sysRelationChanges";
    private static final String UNSAVED_ERR = "Can not relate to an unsaved object.";
    private static final String UNKNOWN_ERR = "Unknown relation: ";

    private Map<String, List<AxObject>> relations;
    private Map<String, List<AxObject>> additions;
    private Map<String, List<AxObject>> removals;

    protected AxRelations(JSONObject properties) {
        this.relations = parse(properties);
        this.additions = new HashMap<>();
        this.removals = new HashMap<>();
    }

    protected List<AxObject> all(String relation) {
        if (this.relations.containsKey(relation)) {
            return this.relations.get(relation);
        } else {
            return new ArrayList<>();
        }
    }

    protected AxObject one(String relation) {
        if (this.relations.containsKey(relation)) {
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

    protected void save() {
        for (List<AxObject> objects : this.relations.values()) {
            for (AxObject object : objects) {
                object.saveAll();
            }
        }
    }

    protected void append(AxObject object) {
        if (additions.isEmpty() && removals.isEmpty()) {
            return;
        }

        Set<String> keys = new HashSet<>();
        keys.addAll(additions.keySet());
        keys.addAll(removals.keySet());

        for (String key : keys) {
            object.put(key, toJSON(
                    additions.get(key),
                    removals.get(key)
            ));
        }
    }

    private List<AxObject> verify(AxObject... objects) {
        for (AxObject object : objects) {
            if (object.getId() == null) {
                throw new AxException(UNSAVED_ERR);
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
            throw new AxException(UNKNOWN_ERR + relation);
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
            changes.put(KEY_ADDITIONS, toJSONArray(additions));
        }

        if (removals != null && !removals.isEmpty()) {
            changes.put(KEY_REMOVALS, toJSONArray(removals));
        }

        JSONObject value = new JSONObject();
        value.put(KEY_CHANGES, changes);
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

        for(Iterator<String> it = properties.keys(); it.hasNext(); ) {
            String key = it.next();
            JSONObject property = parseRelation(properties, key);

            if (property == null) {
                continue;
            }

            relations.put(key, parseObjects(property));
        }

        for (String key : relations.keySet()) {
            properties.remove(key);
        }

        return relations;
    }

    private JSONObject parseRelation(JSONObject properties, String key) {
        if (!(properties.get(key) instanceof JSONObject)) {
            return null;
        }

        JSONObject prop = (JSONObject) properties.get(key);

        if (!prop.has(KEY_RELATIONS) || !prop.getString(KEY_RELATIONS).equals(VAL_RELATIONS)) {
            return null;
        }

        return prop;
    }

    private List<AxObject> parseObjects(JSONObject prop) {
        List<AxObject> objects = new ArrayList<AxObject>();

        JSONArray items = prop.getJSONArray(KEY_OBJECTS);
        String collection = prop.getString(KEY_COLLECTION);

        for (int i = 0; i < items.length(); i++) {
            JSONObject data = items.getJSONObject(i);
            objects.add(new AxObject(collection, data));
        }

        return objects;
    }

}
