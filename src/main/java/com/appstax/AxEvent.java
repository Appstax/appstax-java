package com.appstax;

import org.json.JSONObject;

public class AxEvent {

    private String type;
    private String channel;
    private String message;

    public AxEvent(String type, String channel, String message) {
        this.setType(type);
        this.setChannel(channel);
        this.setMessage(message);
    }

    public String getType() {
        return type;
    }

    public void setType(String event) {
        this.type = event;
    }

    public String getString() {
        return message;
    }

    public JSONObject getJSON() {
        return new JSONObject(getString());
    }

    public AxObject getObject() {
        return AxObject.unmarshal(getString());
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

}
