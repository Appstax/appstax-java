package com.appstax;

import org.json.JSONObject;

public class AxEvent {

    private String channel;
    private JSONObject payload;

    public AxEvent(String channel, JSONObject payload) {
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
        return AxObject.unmarshal(getString());
    }

    public String getChannel() {
        return channel;
    }

}
