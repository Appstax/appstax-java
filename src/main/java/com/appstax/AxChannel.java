package com.appstax;

import com.squareup.okhttp.Response;
import com.squareup.okhttp.ws.WebSocket;
import com.squareup.okhttp.ws.WebSocketListener;
import okio.Buffer;
import okio.BufferedSource;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public final class AxChannel {

    private WebSocket socket;
    private List<JSONObject> queue;
    private AxListener listener;
    private AxClient client;
    private String name;

    protected AxChannel(AxClient client, String name, AxListener listener) {
        this.queue = new CopyOnWriteArrayList<>();
        this.listener = listener;
        this.client = client;
        this.name = name;

        if (this.listener == null) {
            this.listener = new AxListener() {};
        }

        this.validate();
        this.connectAsync();
    }

    public AxChannel send(AxObject object) {
        return send(object.marshal());
    }

    public AxChannel send(JSONObject object) {
        return send(object.toString());
    }

    public AxChannel send(String message) {
        queue.add(item("publish", message));
        flush();
        return this;
    }

    public boolean isOpen() {
        return this.socket != null;
    }

    private void connectAsync() {
        new Thread() {
            public void run() {
                connect();
            }
        }.start();
    }

    private void connect() {
        String id = getSessionId();

        if (id != null) {
            client.socket(
                AxPaths.realtime(id),
                new Dispatcher()
            );
        }
    }

    private String getSessionId() {
        try {
            return client.request(
                AxClient.Method.POST,
                AxPaths.realtimeSessions()
            ).getString("realtimeSessionId");
        } catch (Exception e) {
            listener.onError(e);
            return null;
        }
    }

    private void open(WebSocket socket) {
        queue.add(0, item("subscribe", ""));
        this.socket = socket;
    }

    private void close() {
        this.socket = null;
    }

    private JSONObject item(String command, String message) {
        JSONObject item = new JSONObject();
        item.put("id", messageId());
        item.put("channel", this.name);

        if (!command.isEmpty()) {
            item.put("command", command);
        }

        if (!message.isEmpty()) {
            item.put("message", message);
        }

        return item;
    }

    private void flush() {
        if (isOpen()) {
            for (JSONObject item : this.queue) {
                writeAsync(item);
            }
            this.queue.clear();
        }
    }

    private JSONObject read(BufferedSource source) {
        try {
            String body = source.readUtf8();
            source.close();
            return new JSONObject(body);
        } catch (IOException e) {
            listener.onError(e);
            return null;
        }
    }

    private void writeAsync(final JSONObject item) {
        new Thread() {
            public void run() {
                write(item);
            }
        }.start();
    }

    private void write(JSONObject item) {
        try {
            this.socket.sendMessage(
                    WebSocket.PayloadType.TEXT,
                    new Buffer().writeUtf8(item.toString())
            );
        } catch (IOException e) {
            listener.onError(e);
        }
    }

    private void validate() {
        if (!isPublic() && !isPrivate()) {
            throw new AxException("invalid name " + this.name);
        }
    }

    private boolean isPublic() {
        return this.name.startsWith("public/");
    }

    private boolean isPrivate() {
        return this.name.startsWith("private/");
    }

    private String messageId() {
        return UUID.randomUUID().toString();
    }

    private class Dispatcher implements WebSocketListener {

        @Override
        public void onOpen(WebSocket socket, Response response) {
            open(socket);
            flush();
            listener.onOpen();
        }

        @Override
        public void onFailure(IOException e, Response response) {
            listener.onClose();
        }

        @Override
        public void onMessage(BufferedSource source, WebSocket.PayloadType payloadType) throws IOException {
            try {
                AxEvent event = new AxEvent(client, name, read(source));
                switch (event.getType()) {
                    case "message": listener.onMessage(event); break;
                    case "error":   listener.onError(new AxException(event.getString())); break;
                }
            } catch (Exception e) {
                listener.onError(e);
            }
            flush();
        }

        @Override
        public void onPong(Buffer buffer) {
            flush();
        }

        @Override
        public void onClose(int i, String s) {
            close();
            listener.onClose();
        }

    }

}
