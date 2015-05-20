package com.appstax;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;
import org.junit.Test;

import static org.junit.Assert.*;

public class AppstaxSaveTest extends AppstaxTest {

    @Test
    public void testSaveSuccess() throws Exception {
        MockWebServer server = createMockWebServer();
        String body = getResource("save-object-success.json");
        server.enqueue(new MockResponse().setBody(body));

        AppstaxObject object = new AppstaxObject(COLLECTION_1);
        assertNull(object.getId());
        object.put(PROPERTY_1, "1");

        Appstax.save(object);
        assertNotNull(object.getId());
        assertEquals("1", object.get(PROPERTY_1));

        RecordedRequest req = server.takeRequest();
        assertEquals("POST", req.getMethod());
        assertEquals("/objects/" + COLLECTION_1, req.getPath());
        assertEquals(APP_KEY_1, req.getHeader("x-appstax-appkey"));

        server.shutdown();
    }

    @Test(expected=AppstaxException.class)
    public void testSaveError() throws Exception {
        MockWebServer server = createMockWebServer();
        String body = getResource("save-object-error.json");
        server.enqueue(new MockResponse().setBody(body).setResponseCode(400));

        AppstaxObject object = new AppstaxObject(COLLECTION_1);
        Appstax.save(object);
        server.shutdown();
    }

    @Test
    public void testUpdateSuccess() throws Exception {
        MockWebServer server = createMockWebServer();
        AppstaxObject object = getObject(server);

        String body = getResource("save-object-success.json");
        server.enqueue(new MockResponse().setBody(body));
        object.put(PROPERTY_1, "3");
        Appstax.save(object);

        RecordedRequest req = server.takeRequest();
        assertEquals(object.getId(), "123");
        assertEquals(object.get(PROPERTY_1), "3");
        assertEquals("PUT", req.getMethod());
        assertEquals("/objects/" + COLLECTION_1 + "/" + object.getId(), req.getPath());

        server.shutdown();
    }

    @Test(expected=AppstaxException.class)
    public void testUpdateError() throws Exception {
        MockWebServer server = createMockWebServer();
        AppstaxObject object = getObject(server);

        String body = getResource("save-object-error.json");
        server.enqueue(new MockResponse().setBody(body).setResponseCode(400));
        object.put(PROPERTY_1, "3");
        Appstax.save(object);

        server.shutdown();
    }

}
