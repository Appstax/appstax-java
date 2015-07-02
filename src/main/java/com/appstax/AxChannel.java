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

        new Thread() {
            public void run() {
                create();
            }
        }.start();
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

    public void send(String message) {
        if (name.contains("*")) {
            throw new AxException("can not send to wildcard channel");
        }
        socket.append(this, payload(name, "publish", message));
    }

    public void grant(AxChannel channel, String... users) {
        if (isPrivate(name)) {
            // TODO: grant access
        }
    }

    public void revoke(AxChannel channel, String... users) {
        if (isPrivate(name)) {
            // TODO: revoke access
        }
    }

    protected void onOpen() {
        socket.prepend(this, payload(name, "subscribe", ""));
        listener.onOpen();
    }

    protected void onClose() {
        listener.onClose();
    }

    protected void onError(Exception e) {
        listener.onError(e);
    }

    protected void onEvent(AxEvent event) {
        if (event.getType().equals("error")) {
            listener.onError(new AxException(event.getString()));
        } else {
            listener.onMessage(event);
        }
    }

    private void create() {
        if (isPrivate(name)) {
            // TODO: create private channel
        }
        socket.listen(AxChannel.this);
    }

    private String payload(String name, String cmd, String msg) {
        JSONObject item = new JSONObject();
        item.put("channel", name);
        item.put("id", UUID.randomUUID().toString());
        if (!cmd.isEmpty()) item.put("command", cmd);
        if (!msg.isEmpty()) item.put("message", msg);
        return item.toString();
    }

    private String parse(String name) {
        if (!isPublic(name) && !isPrivate(name)) {
            throw new AxException("invalid name " + name);
        }
        return name;
    }

    protected boolean isPublic(String name) {
        return name.startsWith("public/");
    }

    protected boolean isPrivate(String name) {
        return name.startsWith("private/");
    }

}
