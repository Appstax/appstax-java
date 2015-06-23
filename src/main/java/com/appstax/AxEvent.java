package com.appstax;

public class AxEvent {

    private String type;
    private String channel;
    private String payload;

    public AxEvent(String type, String channel, String payload) {
        this.setType(type);
        this.setChannel(channel);
        this.setPayload(payload);
    }

    public String getType() {
        return type;
    }

    public void setType(String event) {
        this.type = event;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

}
