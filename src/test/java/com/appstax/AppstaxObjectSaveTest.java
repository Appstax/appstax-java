package com.appstax;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;
import org.junit.Test;

import static org.junit.Assert.*;

public class AppstaxObjectSaveTest extends AppstaxTest {

    @Test
    public void testSaveSuccess() throws Exception {
        MockWebServer server = createMockWebServer();
        String body = getResource("save-object-success.json");
        server.enqueue(new MockResponse().setBody(body));

        AppstaxObject object = new AppstaxObject(COLLECTION_1);
        assertNull(object.getId());
        object.put(PROPERTY_1, "1");

        object.save();
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
        object.save();
        server.shutdown();
    }

}
