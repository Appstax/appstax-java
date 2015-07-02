package com.appstax;

import org.json.JSONObject;

import java.util.UUID;

public final class AxChannel {

    private AxSocket socket;
    private AxListener listener;
    private String name;

    protected AxChannel(AxSocket socket, String name, AxListener listener) {
        this.socket = socket;
        this.name = parse(name);
        this.listener = listener;

        if (this.listener == null) {
            this.listener = new AxListener() {};
        }

        this.socket.listen(this);
    }

    public String getName() {
        return this.name;
    }

    public boolean connected() {
        return this.socket.connected();
    }

    public void send(AxObject object) {
        send(object.marshal());
    }

    public void send(JSONObject object) {
        send(object.toString());
    }

    public void send(String message) {
        if (name.contains("*")) {
            throw new AxException("can not send to wildcard channel");
        }
        socket.append(this, payload(name, "publish", message));
    }

    protected void onOpen() {
        socket.prepend(this, payload(name, "subscribe", ""));
        listener.onOpen();
    }

    protected void onClose() {
        listener.onClose();
    }

    protected void onMessage(AxEvent event) {
        listener.onMessage(event);
    }

    protected void onError(Exception e) {
        listener.onError(e);
    }

    private String payload(String name, String cmd, String msg) {
        JSONObject item = new JSONObject();
        item.put("id", messageId());
        item.put("channel", name);
        if (!cmd.isEmpty()) item.put("command", cmd);
        if (!msg.isEmpty()) item.put("message", msg);
        return item.toString();
    }

    private String messageId() {
        return UUID.randomUUID().toString();
    }

    private String parse(String name) {
        if (!isPublic(name) && !isPrivate(name)) {
            throw new AxException("invalid name " + name);
        }
        return name;
    }

    private boolean isPublic(String name) {
        return name.startsWith("public/");
    }

    private boolean isPrivate(String name) {
        return name.startsWith("private/");
    }

}
