package com.appstax;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public final class AxFile {

    private static final String KEY_NAME = "filename";
    private static final String KEY_DATA = "filedata";
    private static final String KEY_URL = "url";

    private String name;
    private byte[] data;
    private String url;
    private boolean saved;

    public AxFile(String name, byte[] data) {
        this.saved = false;
        this.name = name;
        this.data = data;
    }

    protected AxFile(JSONObject meta) {
        this.name = meta.has(KEY_NAME) ? meta.getString(KEY_NAME) : null;
        this.url = meta.has(KEY_URL) ? meta.getString(KEY_URL) : null;
    }

    protected AxFile load() {
        this.data = AxClient.file(AxClient.Method.GET, this.getUrl());
        return this;
    }

    protected AxFile save(String path) {
        if (!this.saved) {
            Map<String, String> form = new HashMap<String, String>();
            form.put(KEY_DATA, this.getData().toString());
            AxClient.form(AxClient.Method.PUT, path, form);
            this.saved = true;
            this.url = path;
        }
        return this;
    }

    public String getName() {
        return name;
    }

    public byte[] getData() {
        return data;
    }

    public String getUrl() {
        return url;
    }

}
