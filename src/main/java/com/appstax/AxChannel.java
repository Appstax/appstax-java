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

    private String name;
    private WebSocket socket;
    private List<JSONObject> queue;
    private AxListener listener;

    protected AxChannel(String name) {
        this.listener = new AxListener() {};
        this.queue = new CopyOnWriteArrayList<>();
        this.name = name;
        this.validateChannel();
        this.connect();
    }

    public AxChannel listen(AxListener listener) {
        this.listener = listener;
        return this;
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

    private void connect() {
        AxClient.socket(
                AxPaths.realtime(getSessionId()),
                new Dispatcher()
        );
    }

    private String getSessionId() {
        return AxClient.request(
            AxClient.Method.POST,
            AxPaths.realtimeSessions()
        ).getString("realtimeSessionId");
    }

    private void open(WebSocket socket) {
        queue.add(0, item("subscribe", ""));
        this.socket = socket;
        flush();
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
                write(item);
            }
            this.queue.clear();
        }
    }

    private void write(JSONObject item) {
        try {
            this.socket.sendMessage(
                WebSocket.PayloadType.TEXT,
                new Buffer().writeUtf8(item.toString())
            );
        } catch (IOException e) {
            throw new AxException(e);
        }
    }

    private void validateChannel() {
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

    private AxEvent parse(BufferedSource source) {
        try {
            String body = source.readUtf8();
            source.close();
            JSONObject json = new JSONObject(body);
            String message = json.getString("message");
            return new AxEvent("message", name, message);
        } catch (IOException e) {
            throw new AxException(e);
        }
    }

    private class Dispatcher implements WebSocketListener {

        @Override
        public void onOpen(WebSocket socket, Response response) {
            open(socket);
            listener.onOpen(new AxEvent("open", name, ""));
        }

        @Override
        public void onFailure(IOException e, Response response) {
            listener.onFailure(new AxEvent("failure", name, e.getMessage()));
        }

        @Override
        public void onMessage(BufferedSource source, WebSocket.PayloadType payloadType) throws IOException {
            listener.onMessage(parse(source));
        }

        @Override
        public void onPong(Buffer buffer) {
            flush();
        }

        @Override
        public void onClose(int i, String s) {
            close();
            listener.onClose(new AxEvent("close", name, s));
        }

    }

}
