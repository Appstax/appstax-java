package com.appstax;

import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import static org.junit.Assert.*;

public class AxSaveTest extends AxTest {

    @org.junit.Test
    public void shouldSaveObject() throws Exception {
        MockWebServer server = createMockWebServer();
        enqueue(1, server, 200, getResource("save-object-success.json"));

        AxObject object = new AxObject(COLLECTION_1);
        assertNull(object.getId());
        object.put(PROPERTY_1, "1");

        Ax.save(object);
        assertNotNull(object.getId());
        assertEquals("1", object.get(PROPERTY_1));

        RecordedRequest req = server.takeRequest();
        assertEquals("POST", req.getMethod());
        assertEquals("/objects/" + COLLECTION_1, req.getPath());
        assertEquals(APP_KEY_1, req.getHeader("x-appstax-appkey"));

        server.shutdown();
    }

    @org.junit.Test(expected=AxException.class)
    public void shouldThrowOnSaveError() throws Exception {
        MockWebServer server = createMockWebServer();
        enqueue(1, server, 400, getResource("save-object-error.json"));

        AxObject object = new AxObject(COLLECTION_1);
        Ax.save(object);
        server.shutdown();
    }

    @org.junit.Test
    public void shouldUpdateObject() throws Exception {
        MockWebServer server = createMockWebServer();
        enqueue(1, server, 200, getResource("save-object-success.json"));

        AxObject object = getObject(server);
        object.put(PROPERTY_1, "3");
        Ax.save(object);

        RecordedRequest req = server.takeRequest();
        assertEquals(object.getId(), "123");
        assertEquals(object.get(PROPERTY_1), "3");
        assertEquals("PUT", req.getMethod());
        assertEquals("/objects/" + COLLECTION_1 + "/" + object.getId(), req.getPath());

        server.shutdown();
    }

    @org.junit.Test(expected=AxException.class)
    public void shouldThrowOnUpdateError() throws Exception {
        MockWebServer server = createMockWebServer();
        enqueue(1, server, 400, getResource("save-object-error.json"));

        AxObject object = getObject(server);
        object.put(PROPERTY_1, "3");
        Ax.save(object);

        server.shutdown();
    }

}
