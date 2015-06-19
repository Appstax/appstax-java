package com.appstax;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public final class AxObject {

    protected static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    protected static final String KEY_CREATED = "sysCreated";
    protected static final String KEY_UPDATED = "sysUpdated";
    protected static final String KEY_ID = "sysObjectId";
    protected static final String KEY_TYPE = "sysDatatype";
    protected static final String KEY_FILE = "filename";
    protected static final String TYPE_FILE = "file";

    private String collection;
    private AxRelations relations;
    private AxPermissions permissions;
    private JSONObject properties;
    private Map<String, AxFile> files;

    public AxObject(String collection) {
        this(collection, new JSONObject());
    }

    public AxObject(String collection, JSONObject properties) {
        this.collection = collection;
        this.properties = properties;
        this.relations = new AxRelations(this.properties);
        this.permissions = new AxPermissions();
        this.files = new HashMap<>();
    }

    public String getCollection() {
        return this.collection;
    }

    public String getId() {
        return this.getString(KEY_ID);
    }

    public String getString(String key) {
        return this.has(key) ? this.properties.getString(key) : null;
    }

    public List<String> getStrings(String key) {
        if (!(this.get(key) instanceof JSONArray)) {
            return null;
        }

        List<String> list = new ArrayList<>();
        JSONArray json = (JSONArray) this.get(key);

        for (int i = 0; i < json.length(); i++) {
            list.add(json.getString(i));
        }

        return list;
    }

    public Object get(String key) {
        return this.has(key) ? this.properties.get(key) : null;
    }

    public Date getCreated() {
        return this.getDate(KEY_CREATED);
    }

    public Date getUpdated() {
        return this.getDate(KEY_UPDATED);
    }

    public Date getDate(String key) {
        if (!this.has(key)) {
            return null;
        }
        try {
            DateFormat source = new SimpleDateFormat(DATE_FORMAT);
            return source.parse(this.getString(key));
        } catch (ParseException e) {
            return null;
        }
    }

    public AxFile getFile(String key) {
        if (!this.has(key)) {
            return null;
        }
        if (!this.files.containsKey(key)) {
            this.files.put(key, fileFromProperty(key));
        }
        return this.files.get(key);
    }

    public void put(String key, Object val) {
        this.properties.put(key, val);
    }

    public void put(String key, Date val) {
        DateFormat target = new SimpleDateFormat(DATE_FORMAT);
        this.put(key, target.format(val));
    }

    public void put(String key, AxFile file) {
        JSONObject meta = new JSONObject();
        meta.put(KEY_TYPE, TYPE_FILE);
        meta.put(KEY_FILE, file.getName());
        this.files.put(key, file);
        this.put(key, meta);
    }

    public boolean has(String key) {
        return this.properties.has(key);
    }

    public AxObject grantPublic(String... permissions) {
        this.permissions.grantPublic(this.getId(), permissions);
        return this;
    }

    public AxObject grant(String username, String... permissions) {
        this.permissions.grant(this.getId(), username, permissions);
        return this;
    }

    public AxObject revokePublic(String... permissions) {
        this.permissions.revokePublic(this.getId(), permissions);
        return this;
    }

    public AxObject revoke(String username, String... permissions) {
        this.permissions.revoke(this.getId(), username, permissions);
        return this;
    }

    public AxObject getObject(String relation) {
        return this.relations.one(relation);
    }

    public List<AxObject> getObjects(String relation) {
        return this.relations.all(relation);
    }

    public AxObject createRelation(String relation, AxObject... additions) {
        this.relations.createRelation(relation, additions);
        return this;
    }

    public AxObject removeRelation(String relation, AxObject... removals) {
        this.relations.removeRelation(relation, removals);
        return this;
    }

    protected AxObject save() {
        relations.appendChanges(this);
        saveObject();
        relations.removeChanges(this);
        permissions.save();
        saveFiles();
        return this;
    }

    protected Set<AxObject> flatten(Set<AxObject> objects) {
        if (objects == null) {
            objects = new HashSet<>();
        }
        if (!objects.contains(this)) {
            objects.add(this);
            this.relations.flatten(objects);
        }
        return objects;
    }

    protected AxObject remove() {
        String path = AxPaths.object(this.getCollection(), this.getId());
        this.properties = AxClient.request(AxClient.Method.DELETE, path);
        return this;
    }

    protected AxObject refresh() {
        String path = AxPaths.object(this.getCollection(), this.getId());
        this.properties = AxClient.request(AxClient.Method.GET, path);
        return this;
    }

    private void saveObject() {
        if (this.getId() == null) {
            this.createObject();
        } else {
            this.updateObject();
        }
    }

    private AxObject createObject() {
        String path = AxPaths.collection(this.getCollection());
        JSONObject meta = AxClient.request(AxClient.Method.POST, path, this.properties);
        this.put(KEY_CREATED, meta.get(KEY_CREATED));
        this.put(KEY_UPDATED, meta.get(KEY_UPDATED));
        this.put(KEY_ID, meta.get(KEY_ID));
        return this;
    }

    private AxObject updateObject() {
        String path = AxPaths.object(this.getCollection(), this.getId());
        JSONObject meta = AxClient.request(AxClient.Method.PUT, path, this.properties);
        this.put(KEY_UPDATED, meta.get(KEY_UPDATED));
        return this;
    }

    private void saveFiles() {
        for (Map.Entry<String, AxFile> item : this.files.entrySet()) {
            String key = item.getKey();
            AxFile file = item.getValue();
            String path = AxPaths.file(this.getCollection(), this.getId(), key, file.getName());
            file.save(path);
        }
    }

    private AxFile fileFromProperty(String key) {
        JSONObject meta = this.properties.getJSONObject(key);
        if (!meta.getString(KEY_TYPE).equals(TYPE_FILE)) return null;
        return new AxFile(meta);
    }

}
