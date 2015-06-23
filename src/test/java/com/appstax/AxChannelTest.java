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

        server.enqueue(new MockResponse().setBody(getResource("user-session-success.json")));
        server.enqueue(new MockResponse().withWebSocketUpgrade(new MessageRecorder(lock, res)));

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
    public void send() throws Exception {
        final CountDownLatch lock = new CountDownLatch(2);
        final AtomicReference<String> res = new AtomicReference<>();

        server.enqueue(new MockResponse().setBody(getResource("user-session-success.json")));
        server.enqueue(new MockResponse().withWebSocketUpgrade(new MessageRecorder(lock, res)));

        AxChannel channel = Ax.channel("public/chat");
        channel.send("123");
        lock.await();

        JSONObject msg = new JSONObject(res.get());
        assertEquals("123", msg.getString("message"));
    }

    @Test
    public void receive() throws Exception {
        final CountDownLatch lock = new CountDownLatch(1);
        final AtomicReference<AxEvent> res = new AtomicReference<>();

        server.enqueue(new MockResponse().setBody(getResource("user-session-success.json")));
        server.enqueue(new MockResponse().withWebSocketUpgrade(new MessageSender("321")));

        Ax.channel("public/chat").listen(new AxListener() {
            void onMessage(AxEvent event) {
                res.set(event);
                lock.countDown();
            }
        });

        lock.await();
        assertNotNull(res.get());
        assertEquals("321", res.get().getPayload());
    }

    protected static class EmptyListener implements WebSocketListener {

        @Override
        public void onMessage(BufferedSource payload, WebSocket.PayloadType type) throws IOException {}

        @Override
        public void onOpen(WebSocket socket, Response response) {}

        @Override
        public void onPong(Buffer payload) {}

        @Override
        public void onClose(int code, String reason) {}

        @Override
        public void onFailure(IOException e, Response response) {}

    }

    protected static class MessageRecorder extends EmptyListener {

        final CountDownLatch lock;
        final AtomicReference<String> res;

        public MessageRecorder(CountDownLatch lock, AtomicReference<String> res) {
            super();
            this.lock = lock;
            this.res = res;
        }

        @Override
        public void onMessage(BufferedSource payload, WebSocket.PayloadType type) throws IOException {
            res.set(payload.readUtf8());
            lock.countDown();
            payload.close();
        }

    }

    protected static class MessageSender extends EmptyListener {

        final String msg;

        public MessageSender(String msg) {
            super();
            this.msg = msg;
        }

        @Override
        public void onOpen(final WebSocket socket, final Response response) {
            new Thread() {

                @Override
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
