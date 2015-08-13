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
        ax.channel("invalid", null);
    }

    @Test
    public void subscribe() throws Exception {
        final CountDownLatch lock = new CountDownLatch(1);
        final AtomicReference<String> res = new AtomicReference<>();
        socket(new Recorder(lock, res, "subscribe"));

        AxChannel c1 = ax.channel("public/sub1", null);
        AxChannel c2 = ax.channel("public/sub2", null);
        assertFalse(c1.connected());
        assertFalse(c2.connected());

        lock.await();
        assertTrue(c1.connected());
        assertTrue(c2.connected());

        JSONObject msg = new JSONObject(res.get());
        assertTrue(msg.getString("channel").startsWith("public/sub"));
        assertEquals("subscribe", msg.get("command"));
        assertFalse(msg.has("message"));
        assertTrue(msg.has("id"));
    }

    @Test(expected=AxException.class)
    public void sendWildcard() throws Exception {
        socket(null);
        ax.channel("public/*", null).send("1");
    }

    @Test
    public void receiveWildcard() throws Exception {
        final CountDownLatch lock = new CountDownLatch(4*3-1);
        socket(new Sender(getResource("channel-receive-wildcard.json")));

        AxListener count = new AxListener() {
            public void onMessage(AxEvent event) {
                lock.countDown();
            }
        };
        AxListener fail = new AxListener() {
            public void onMessage(AxEvent event) {
                throw new AssertionError();
            }
        };

        for (int i = 0; i < 3; i++) {
            ax.channel("public/*", count);
            ax.channel("public/im/*", count);
            ax.channel("public/im/*", count);
            ax.channel("public/im/f*", count);

            ax.channel("public/", fail);
            ax.channel("public/im/", fail);
            ax.channel("public/im/f", fail);
            ax.channel("public/im/foo/", fail);
        }

        lock.await();
    }

    @Test
    public void sendString() throws Exception {
        final CountDownLatch lock = new CountDownLatch(1);
        final AtomicReference<String> res = new AtomicReference<>();
        socket(new Recorder(lock, res, "publish"));

        ax.channel("public/sendString", null).send("123");
        lock.await();
        assertTrue(res.get().contains("\"message\":\"123\""));
    }

    @Test
    public void receiveString() throws Exception {
        final CountDownLatch lock = new CountDownLatch(1);
        final AtomicReference<AxEvent> res = new AtomicReference<>();
        socket(new Sender(getResource("channel-receive-string.json")));

        ax.channel("public/receiveString", new AxListener() {
            public void onMessage(AxEvent event) {
                res.set(event);
                lock.countDown();
            }
        });

        lock.await();
        assertEquals("321", res.get().getString());
        assertEquals("message", res.get().getType());
    }

    @Test
    public void sendObject() throws Exception {
        final CountDownLatch lock = new CountDownLatch(1);
        final AtomicReference<String> res = new AtomicReference<>();
        socket(new Recorder(lock, res, "publish"));

        AxObject object = ax.object(COLLECTION_1);
        ax.channel("public/sendObject", null).send(object);
        lock.await();

        String act = res.get();
        String exp = "\\\"collection\\\":\\\"" + COLLECTION_1 + "\\\"";
        assertTrue(act.contains(exp));
    }

    @Test
    public void receiveObject() throws Exception {
        final CountDownLatch lock = new CountDownLatch(1);
        final AtomicReference<AxEvent> res = new AtomicReference<>();
        socket(new Sender(getResource("channel-receive-object.json")));

        ax.channel("public/receiveObject", new AxListener() {
            public void onMessage(AxEvent event) {
                res.set(event);
                lock.countDown();
            }
        });

        lock.await();
        AxObject result = res.get().getObject();
        assertEquals(COLLECTION_1, result.getCollection());
        assertEquals("bar", result.getString("foo"));
    }

    private void socket(WebSocketListener listener) throws IOException {
        server.enqueue(new MockResponse().setBody(getResource("user-session-success.json")));
        server.enqueue(new MockResponse().withWebSocketUpgrade(listener));
    }

    protected static class Recorder extends EmptyListener {

        final CountDownLatch lock;
        final AtomicReference<String> res;
        final String pattern;

        public Recorder(CountDownLatch lock, AtomicReference<String> res, String pattern) {
            super();
            this.pattern = pattern;
            this.lock = lock;
            this.res = res;
        }

        @Override
        public void onMessage(BufferedSource source, WebSocket.PayloadType type) throws IOException {
            String payload = source.readUtf8();

            if (payload.contains(pattern)) {
                res.set(payload);
                lock.countDown();
            }

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

}
