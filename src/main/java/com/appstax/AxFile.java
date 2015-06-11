package com.appstax;

import org.json.JSONObject;

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
        this.data = AxClient.file(AxClient.Method.GET, this.url);
        return this;
    }

    protected AxFile save(String path) {
        if (!this.saved) {
            AxClient.multipart(AxClient.Method.PUT, path, KEY_DATA, getName(), getData());
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
        if (this.url == null) {
            return null;
        } else {
            return Ax.getApiUrl() + this.url;
        }
    }

}
