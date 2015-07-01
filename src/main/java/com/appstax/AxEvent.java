package com.appstax;

import org.json.JSONObject;

public class AxEvent {

    private AxClient client;
    private String channel;
    private JSONObject payload;

    protected AxEvent(AxClient client, String channel, JSONObject payload) {
        this.client = client;
        this.channel = channel;
        this.payload = payload;
    }

    public String getType() {
        return payload.getString("event");
    }

    public String getString() {
        return payload.getString("message");
    }

    public JSONObject getJSON() {
        return payload.getJSONObject("message");
    }

    public AxObject getObject() {
        return AxObject.unmarshal(client, getString());
    }

    public String getChannel() {
        return channel;
    }

}
