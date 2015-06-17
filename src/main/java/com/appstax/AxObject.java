package com.appstax;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public final class AxObject {

    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    private static final String KEY_CREATED = "sysCreated";
    private static final String KEY_UPDATED = "sysUpdated";
    private static final String KEY_ID = "sysObjectId";
    private static final String KEY_TYPE = "sysDatatype";

    private static final String KEY_USER = "username";
    private static final String KEY_FILE = "filename";
    private static final String TYPE_FILE = "file";

    private static final String KEY_GRANTS = "grants";
    private static final String KEY_REVOKES = "revokes";
    private static final String KEY_PERMISSIONS = "permissions";

    private static final String KEY_ADDITIONS = "additions";
    private static final String KEY_REMOVALS = "removals";
    private static final String KEY_RELATIONS = "sysRelationChanges";

    private String collection;
    private JSONObject properties;
    private JSONObject relations;
    private JSONObject access;
    private Map<String, AxFile> files;

    public AxObject(String collection) {
        this(collection, new JSONObject());
    }

    public AxObject(String collection, JSONObject properties) {
        this.collection = collection;
        this.properties = properties;
        this.access = new JSONObject();
        this.access.put(KEY_GRANTS, new JSONArray());
        this.access.put(KEY_REVOKES, new JSONArray());
        this.files = new HashMap<String, AxFile>();
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
        return this.permission(KEY_GRANTS, "*", permissions);
    }

    public AxObject grant(String username, String... permissions) {
        return this.permission(KEY_GRANTS, username, permissions);
    }

    public AxObject revokePublic(String... permissions) {
        return this.permission(KEY_REVOKES, "*", permissions);
    }

    public AxObject revoke(String username, String... permissions) {
        return this.permission(KEY_REVOKES, username, permissions);
    }

    public AxObject createRelation(String relation, String... additions) {
        return this.relation(relation, KEY_ADDITIONS, additions);
    }

    public AxObject removeRelation(String relation, String... removals) {
        return this.relation(relation, KEY_REMOVALS, removals);
    }

    protected AxObject save() {
        saveObject();
        saveAccess();
        saveFiles();
        return this;
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

    private void saveAccess() {
        if (this.hasAccess()) {
            String path = AxPaths.permissions();
            AxClient.request(AxClient.Method.POST, path, this.access);
        }
    }

    private boolean hasAccess() {
        return (
            this.access.getJSONArray(KEY_GRANTS).length() > 0 ||
            this.access.getJSONArray(KEY_REVOKES).length() > 0
        );
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

    private AxObject permission(String type, String username, String[] items) {
        if (items.length == 0) {
            return this;
        }
        JSONObject grant = new JSONObject();
        grant.put(KEY_ID, this.getId());
        grant.put(KEY_USER, username);
        grant.put(KEY_PERMISSIONS, new JSONArray(items));
        this.access.getJSONArray(type).put(grant);
        return this;
    }

    private AxObject relation(String relation, String type, String[] items) {
        if (items.length == 0) {
            return this;
        }

        if (this.get(relation) == null) {
            JSONObject value = new JSONObject();
            JSONObject changes = new JSONObject();
            changes.put(KEY_ADDITIONS, new JSONArray());
            changes.put(KEY_REMOVALS, new JSONArray());
            value.put(KEY_RELATIONS, changes);
            this.put(relation, value);
        }

        JSONObject value = (JSONObject) this.get(relation);
        JSONArray changes = value.getJSONObject(KEY_RELATIONS).getJSONArray(type);

        for (String id : items) {
            changes.put(id);
        }

        return this;
    }

}
