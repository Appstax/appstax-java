package com.appstax;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.UUID;

public final class AxChannel {

    private AxSocket socket;
    private AxListener listener;
    private String filter;
    private String name;

    protected AxChannel(AxSocket socket, String name) {
        this(socket, name, null);
    }

    protected AxChannel(AxSocket socket, String name, String filter) {
        this.socket = socket;
        this.filter = filter;
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public AxChannel create() {
        socket.append(this, payload(name, "channel.create", null));
        return this;
    }

    public AxChannel delete() {
        socket.append(this, payload(name, "channel.delete", null));
        return this;
    }

    public AxChannel listen(AxListener listener) {
        this.listener = listener;

        if (this.listener == null) {
            this.listener = new AxListener() {};
        }

        new Thread() {
            public void run() {
                socket.listen(AxChannel.this);
            }
        }.start();

        return this;
    }

    public boolean connected() {
        return this.socket.connected();
    }

    public AxChannel send(AxObject object) {
        if (object == null) {
            return this;
        }

        send(object.marshal());
        return this;
    }

    public AxChannel send(String message) {
        if (name.contains("*")) {
            throw new AxException("can not send to wildcard channel");
        }

        if (message.equals("")) {
            return this;
        }

        socket.append(this, payload(name, "publish", message));
        return this;
    }

    public AxChannel grant(String permission, String... users) {
        this.permission("channel.grant." + permission, users);
        return this;
    }

    public AxChannel revoke(String permission, String... users) {
        this.permission("channel.revoke." + permission, users);
        return this;
    }

    protected void onOpen() {
        socket.prepend(this, payload(name, "subscribe", null));
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

    private void permission(String permission, String[] users) {
        JSONArray json = new JSONArray(Arrays.asList(users));
        socket.append(this, payload(name, permission, json));
    }

    private String payload(String name, String cmd, Object data) {
        JSONObject item = new JSONObject();

        item.put("command", cmd);
        item.put("channel", name);
        item.put("id", UUID.randomUUID().toString());

        if (data != null) {
            item.put("data", data);
        }

        if (filter != null) {
            item.put("filter", filter);
        }

        return item.toString();
    }

}
