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
        return payload.optString("event");
    }

    public String getChannel() {
        return payload.optString("channel");
    }

    public String getString() {
        return payload.optString("message");
    }

    public AxObject getObject() {
        String msg = this.getString();

        if (!msg.startsWith("{")) {
            return null;
        }

        return AxObject.unmarshal(client, msg);
    }

}
