package com.appstax;

import org.json.JSONObject;

public class AxEvent {

    private AxClient client;
    private JSONObject payload;

    protected AxEvent(AxClient client, JSONObject payload) {
        this.client = client;
        this.payload = payload;
    }

    public String getType() {
        return payload.getString("event");
    }

    public String getChannel() {
        return payload.getString("channel");
    }

    public String getString() {
        return payload.getString("message");
    }

    public AxObject getObject() {
        return AxObject.unmarshal(client, getString());
    }

}
