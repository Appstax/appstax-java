package com.appstax;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import static org.junit.Assert.*;

public class AxSaveTest extends AxTest {

    @org.junit.Test
    public void testSaveSuccess() throws Exception {
        MockWebServer server = createMockWebServer();
        String body = getResource("save-object-success.json");
        server.enqueue(new MockResponse().setBody(body));

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
    public void testSaveError() throws Exception {
        MockWebServer server = createMockWebServer();
        String body = getResource("save-object-error.json");
        server.enqueue(new MockResponse().setBody(body).setResponseCode(400));

        AxObject object = new AxObject(COLLECTION_1);
        Ax.save(object);
        server.shutdown();
    }

    @org.junit.Test
    public void testUpdateSuccess() throws Exception {
        MockWebServer server = createMockWebServer();
        AxObject object = getObject(server);

        String body = getResource("save-object-success.json");
        server.enqueue(new MockResponse().setBody(body));
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
    public void testUpdateError() throws Exception {
        MockWebServer server = createMockWebServer();
        AxObject object = getObject(server);

        String body = getResource("save-object-error.json");
        server.enqueue(new MockResponse().setBody(body).setResponseCode(400));
        object.put(PROPERTY_1, "3");
        Ax.save(object);

        server.shutdown();
    }

}
