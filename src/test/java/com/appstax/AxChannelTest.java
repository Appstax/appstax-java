package com.appstax;

import com.squareup.okhttp.Response;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.ws.WebSocket;
import com.squareup.okhttp.ws.WebSocketListener;
import okio.Buffer;
import okio.BufferedSource;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static com.squareup.okhttp.ws.WebSocket.PayloadType.TEXT;
import static org.junit.Assert.*;

public class AxChannelTest extends AxTest {

    @Test(expected=AxException.class)
    public void name() {
        Ax.channel("foo", null);
    }

    @Test
    public void subscribe() throws Exception {
        final CountDownLatch lock = new CountDownLatch(1);
        final AtomicReference<String> res = new AtomicReference<>();
        socket(new Recorder(lock, res));

        AxChannel channel = Ax.channel("public/chat", null);
        assertFalse(channel.isOpen());
        lock.await();
        assertTrue(channel.isOpen());

        JSONObject msg = new JSONObject(res.get());
        assertEquals("public/chat", msg.get("channel"));
        assertEquals("subscribe", msg.get("command"));
        assertFalse(msg.has("message"));
        assertTrue(msg.has("id"));
    }

    @Test
    public void sendString() throws Exception {
        final CountDownLatch lock = new CountDownLatch(1);
        final AtomicReference<AxEvent> res = new AtomicReference<>();
        socket(new Sender(getResource("channel-message-string.json")));

        Ax.channel("public/chat", new AxListener() {
            public void onMessage(AxEvent event) {
                res.set(event);
                lock.countDown();
            }
        }).send("321");

        lock.await();
        assertEquals("321", res.get().getString());
        assertEquals("message", res.get().getType());
    }

    @Test
    public void sendObject() throws Exception {
        final CountDownLatch lock = new CountDownLatch(1);
        final AtomicReference<AxEvent> res = new AtomicReference<>();
        socket(new Sender(getResource("channel-message-object.json")));

        AxObject object = new AxObject(COLLECTION_1);
        object.put("foo", "bar");

        Ax.channel("public/chat", new AxListener() {
            public void onMessage(AxEvent event) {
                res.set(event);
                lock.countDown();
            }
        }).send(object);

        lock.await();
        AxObject result = res.get().getObject();
        assertEquals(COLLECTION_1, result.getCollection());
        assertEquals("bar", result.getString("foo"));
    }

    private void socket(WebSocketListener listener) throws IOException {
        server.enqueue(new MockResponse().setBody(getResource("user-session-success.json")));
        server.enqueue(new MockResponse().withWebSocketUpgrade(listener));
    }

    protected static class EmptyListener implements WebSocketListener {

        @Override
        public void onMessage(BufferedSource source, WebSocket.PayloadType type) throws IOException {}

        @Override
        public void onOpen(WebSocket socket, Response response) {}

        @Override
        public void onPong(Buffer payload) {}

        @Override
        public void onClose(int code, String reason) {}

        @Override
        public void onFailure(IOException e, Response response) {}

    }

    protected static class Recorder extends EmptyListener {

        final CountDownLatch lock;
        final AtomicReference<String> res;

        public Recorder(CountDownLatch lock, AtomicReference<String> res) {
            super();
            this.lock = lock;
            this.res = res;
        }

        @Override
        public void onMessage(BufferedSource source, WebSocket.PayloadType type) throws IOException {
            res.set(source.readUtf8());
            lock.countDown();
            source.close();
        }

    }

    protected static class Sender extends EmptyListener {

        final String payload;

        public Sender(String payload) {
            this.payload = payload;
        }

        @Override
        public void onMessage(BufferedSource source, WebSocket.PayloadType type) throws IOException {
            source.close();
        }

        @Override
        public void onOpen(final WebSocket socket, final Response response) {
            new Thread() {
                public void run() {
                    try {
                        Thread.sleep(100);
                        socket.sendMessage(TEXT, new Buffer().writeUtf8(payload));
                    } catch (IOException | InterruptedException e) {
                        throw new AssertionError(e);
                    }
                }
            }.start();

        }

    }

}
