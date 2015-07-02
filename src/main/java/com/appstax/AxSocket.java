package com.appstax;

import com.squareup.okhttp.Response;
import com.squareup.okhttp.ws.WebSocket;
import com.squareup.okhttp.ws.WebSocketListener;
import okio.Buffer;
import okio.BufferedSource;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

class AxSocket implements WebSocketListener {

    private AxClient client;
    private WebSocket socket;
    private List<AxChannel> channels;
    private List<Item> queue;
    private boolean connecting;

    protected AxSocket(AxClient client) {
        this.channels = new CopyOnWriteArrayList<>();
        this.queue = new CopyOnWriteArrayList<>();
        this.client = client;
    }

    protected boolean connected() {
        return this.socket != null;
    }

    protected void listen(AxChannel channel) {
        this.channels.add(channel);
        open();
    }

    protected void prepend(AxChannel channel, String payload) {
        queue.add(0, new Item(channel, payload));
        flush();
    }

    protected void append(AxChannel channel, String payload) {
        queue.add(new Item(channel, payload));
        flush();
    }

    private void open() {
        if (!connected() && !connecting) {
            connect();
        }
    }

    private void connect() {
        connecting = true;
        new Thread() {
            public void run() {
                getSocket(getSessionId());
                connecting = false;
            }
        }.start();
    }

    private void getSocket(String id) {
        if (id != null) {
            client.socket(AxPaths.realtime(id), this);
        }
    }

    private String getSessionId() {
        try {
            return client.request(
                AxClient.Method.POST,
                AxPaths.realtimeSessions()
            ).getString("realtimeSessionId");
        } catch (Exception e) {
            onErrorAll(e);
            return null;
        }
    }

    private void flush() {
        if (connected()) {
            for (Item item : this.queue) {
                write(item);
            }
            this.queue.clear();
        }
    }

    private void write(final Item item) {
        new Thread() {
            public void run() {
                try {
                    socket.sendMessage(
                        WebSocket.PayloadType.TEXT,
                        new Buffer().writeUtf8(item.payload)
                    );
                } catch (IOException e) {
                    item.channel.onError(e);
                }
            }
        }.start();
    }

    private void onOpenAll() {
        for (AxChannel channel : channels) {
            channel.onOpen();
        }
    }

    private void onCloseAll() {
        for (AxChannel channel : channels) {
            channel.onClose();
        }
    }

    private void onErrorAll(Exception e) {
        for (AxChannel channel : channels) {
            channel.onError(e);
        }
    }

    private void onMessageAll(AxEvent event) {
        for (AxChannel channel : channels) {
            if (match(channel, event)) {
                switch (event.getType()) {
                    case "message": channel.onMessage(event); break;
                    case "error":   channel.onError(new AxException(event.getString())); break;
                }
            }
        }
    }

    private boolean match(AxChannel channel, AxEvent event) {
        String source = event.getChannel();
        String target = channel.getName();

        if (source.equals(target)) return true;
        if (!target.endsWith("*")) return false;

        String base = target.substring(0, target.length() - 1);
        return source.startsWith(base);
    }

    @Override
    public void onMessage(BufferedSource source, WebSocket.PayloadType payloadType) {
        try {
            String payload = source.readUtf8();
            JSONObject item = new JSONObject(payload);
            onMessageAll(new AxEvent(client, item));
            source.close();
        } catch (IOException e) {
            onErrorAll(e);
        }
    }

    @Override
    public void onOpen(WebSocket socket, Response response) {
        AxSocket.this.socket = socket;
        onOpenAll();
        flush();
    }

    @Override
    public void onFailure(IOException e, Response response) {
        onErrorAll(e);
    }

    @Override
    public void onPong(Buffer buffer) {
        flush();
    }

    @Override
    public void onClose(int i, String s) {
        socket = null;
        onCloseAll();
    }

    private class Item {

        private AxChannel channel;
        private String payload;

        public Item(AxChannel channel, String payload) {
            this.channel = channel;
            this.payload = payload;
        }

    }

}
