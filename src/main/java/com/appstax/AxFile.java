package com.appstax;

import org.json.JSONObject;

public final class AxFile {

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
        this.name = meta.has("filename") ? meta.getString("filename") : null;
        this.url = meta.has("url") ? Ax.getApiUrl() + meta.getString("url") : null;
    }

    protected AxFile load() {
        this.data = AxClient.file(AxClient.Method.GET, this.url);
        return this;
    }

    protected AxFile save(String path) {
        if (!this.saved) {
            AxClient.multipart(AxClient.Method.PUT, path, "filedata", getName(), getData());
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
        return this.url;
    }

}
