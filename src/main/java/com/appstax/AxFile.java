package com.appstax;

import org.json.JSONObject;

public class AxFile {

    private String name;
    private String data;
    private String url;

    public AxFile(String name, String data) {
        this.name = name;
        this.data = data;
    }

    protected AxFile(JSONObject properties) {
        this.name = properties.getString("filename");
        this.url = properties.getString("url");
    }

    protected AxFile load() {
        this.data = AxClient.file(AxClient.Method.GET, this.getUrl());
        return this;
    }

    public String getName() {
        return name;
    }

    public String getData() {
        return data;
    }

    public String getUrl() {
        return url;
    }
}
