package com.appstax;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public final class AxObject {

    protected static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    protected static final String UNSAVED_ERR = "Can not relate to an unsaved object.";

    protected static final String KEY_CREATED = "sysCreated";
    protected static final String KEY_UPDATED = "sysUpdated";
    protected static final String KEY_ID = "sysObjectId";
    protected static final String KEY_TYPE = "sysDatatype";
    protected static final String KEY_FILE = "filename";
    protected static final String TYPE_FILE = "file";

    protected static final String KEY_ADDITIONS = "additions";
    protected static final String KEY_REMOVALS = "removals";
    protected static final String KEY_RELATIONS = "sysRelationChanges";

    private String collection;
    private AxPermissions permissions;
    private JSONObject properties;
    private Map<String, AxFile> files;
    private Map<String, List<AxObject>> relations;

    public AxObject(String collection) {
        this(collection, new JSONObject());
    }

    public AxObject(String collection, JSONObject properties) {
        this.collection = collection;
        this.properties = properties;
        this.permissions = new AxPermissions();
        this.files = new HashMap<>();
        this.relations = new HashMap<>();
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

    public AxObject createRelation(String relation, AxObject... additions) {
        this.marshalRelations(relation, KEY_ADDITIONS, additions);

        if (this.relations.get(relation) == null) {
            this.relations.put(relation, new ArrayList<AxObject>(Arrays.asList(additions)));
        } else {
            this.relations.get(relation).addAll(Arrays.asList(additions));
        }

        return this;
    }

    public AxObject removeRelation(String relation, AxObject... removals) {
        if (this.relations.get(relation) == null) {
            throw new AxException("Unknown relation: " + relation);
        }

        this.marshalRelations(relation, KEY_REMOVALS, removals);

        for (AxObject removal : removals) {
            for (AxObject existing : this.relations.get(relation)) {
                if (existing.getId().equals(removal.getId())) {
                    this.relations.get(relation).remove(existing);
                }
            }
        }

        return this;
    }

    protected AxObject save() {
        saveObject();
        permissions.save();
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

    private AxObject marshalRelations(String relation, String type, AxObject[] objects) {
        if (objects.length == 0) {
            return this;
        }

        for (AxObject object : objects) {
            if (object.getId() == null) {
                throw new AxException(UNSAVED_ERR);
            }
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

        for (AxObject object : objects) {
            changes.put(object.getId());
        }

        return this;
    }

}
