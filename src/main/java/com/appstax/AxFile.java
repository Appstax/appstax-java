package com.appstax;

import org.json.JSONObject;

public final class AxFile {

    private AxClient client;
    private String name;
    private byte[] data;
    private String url;
    private boolean saved;

    protected AxFile(AxClient client, String name, byte[] data) {
        this.client = client;
        this.saved = false;
        this.name = name;
        this.data = data;
    }

    protected AxFile(AxClient client, JSONObject meta) {
        this(client, meta.getString("filename"), null);

        if (meta.has("url")) {
            this.url = client.getUrl() + meta.getString("url");
        }
    }

    protected AxFile load() {
        this.data = client.file(AxClient.Method.GET, this.url);
        return this;
    }

    protected AxFile save(String path) {
        if (!this.saved) {
            client.multipart(AxClient.Method.PUT, path, "filedata", getName(), getData());
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
        if (url == null) {
            return null;
        }
        if (url.startsWith("http")) {
            return url;
        }
        return client.getUrl() + url;
    }

}
