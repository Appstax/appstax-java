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
        Ax.channel("foo");
    }

    @Test
    public void subscribe() throws Exception {
        final CountDownLatch lock = new CountDownLatch(1);
        final AtomicReference<String> res = new AtomicReference<>();
        socket(new RecordListener(lock, res));

        AxChannel channel = Ax.channel("public/chat");
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
        socket(new EchoListener());

        Ax.channel("public/chat").listen(new AxListener() {
            void onMessage(AxEvent event) {
                res.set(event);
                lock.countDown();
            }
        }).send("123");

        lock.await();
        assertEquals("123", res.get().getString());
    }

    @Test
    public void sendObject() throws Exception {
        final CountDownLatch lock = new CountDownLatch(1);
        final AtomicReference<AxEvent> res = new AtomicReference<>();
        socket(new EchoListener());

        AxObject object = new AxObject(COLLECTION_1);
        object.put("foo", "bar");

        Ax.channel("public/chat").listen(new AxListener() {
            void onMessage(AxEvent event) {
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

    protected static class RecordListener extends EmptyListener {

        final CountDownLatch lock;
        final AtomicReference<String> res;

        public RecordListener(CountDownLatch lock, AtomicReference<String> res) {
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

    protected static class EchoListener extends EmptyListener {

        private WebSocket socket;

        @Override
        public void onOpen(final WebSocket socket, final Response response) {
            this.socket = socket;
        }

        @Override
        public void onMessage(final BufferedSource source, WebSocket.PayloadType type) throws IOException {
            String msg = source.readUtf8();
            source.close();
            if (isMessage(msg)) send(msg);
        }

        private boolean isMessage(String msg) {
            return new JSONObject(msg).has("message");
        }

        private void send(final String msg) {
            new Thread() {
                public void run() {
                    try {
                        socket.sendMessage(TEXT, new Buffer().writeUtf8(msg));
                    } catch (IOException e) {
                        throw new AssertionError(e);
                    }
                }
            }.start();
        }

    }

}
