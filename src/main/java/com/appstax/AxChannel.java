package com.appstax;

import com.squareup.okhttp.Response;
import com.squareup.okhttp.ws.WebSocket;
import com.squareup.okhttp.ws.WebSocketListener;
import okio.Buffer;
import okio.BufferedSource;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class AxChannel {

    private static final String KEY_SESSION = "realtimeSessionId";
    private static final String KEY_MESSAGE_ID = "id";
    private static final String KEY_COMMAND = "command";
    private static final String KEY_CHANNEL = "channel";
    private static final String KEY_MESSAGE = "message";

    private static final String CMD_PUBLISH = "publish";
    private static final String CMD_SUBSCRIBE = "subscribe";
    private static final String PREFIX_PUBLIC = "public/";
    private static final String PREFIX_PRIVATE = "private/";

    private String name;
    private WebSocket socket;
    private List<JSONObject> queue;
    private AxListener listener;

    protected AxChannel(String name) {
        this.listener = new AxListener() {};
        this.queue = new ArrayList<>();
        this.name = name;
        this.validateChannel();
        this.connect();
    }

    public void listen(AxListener listener) {
        this.listener = listener;
    }

    public void send(String message) {
        queue.add(item(CMD_PUBLISH, message));
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
        ).getString(KEY_SESSION);
    }

    private void open(WebSocket socket) {
        this.socket = socket;
    }

    private void close() {
        this.socket = null;
    }

    private JSONObject item(String command, String message) {
        JSONObject item = new JSONObject();

        item.put(KEY_MESSAGE_ID, messageId());
        item.put(KEY_CHANNEL, this.name);

        if (!command.isEmpty()) {
            item.put(KEY_COMMAND, command);
        }

        if (!message.isEmpty()) {
            item.put(KEY_MESSAGE, message);
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
        return this.name.startsWith(PREFIX_PUBLIC);
    }

    private boolean isPrivate() {
        return this.name.startsWith(PREFIX_PRIVATE);
    }

    private String messageId() {
        return UUID.randomUUID().toString();
    }

    private class Dispatcher implements WebSocketListener {

        @Override
        public void onOpen(WebSocket socket, Response response) {
            queue.add(0, item(CMD_SUBSCRIBE, ""));
            open(socket);
            flush();
            listener.onOpen(new AxEvent("open", name, ""));
        }

        @Override
        public void onFailure(IOException e, Response response) {
            listener.onFailure(new AxEvent("failure", name, e.getMessage()));
        }

        @Override
        public void onMessage(BufferedSource bufferedSource, WebSocket.PayloadType payloadType) throws IOException {
            if (payloadType == WebSocket.PayloadType.TEXT) {
                String body = bufferedSource.readUtf8();
                AxEvent event = new AxEvent("message", name, body);
                listener.onMessage(event);
                bufferedSource.close();
            }
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
