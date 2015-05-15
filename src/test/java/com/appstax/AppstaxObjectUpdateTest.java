package com.appstax;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AppstaxObjectUpdateTest extends AppstaxTest {

    @Test
    public void testUpdateSuccess() throws Exception {
        MockWebServer server = createMockWebServer();
        AppstaxObject object = getObject(server);

        String body = getResource("save-object-success.json");
        server.enqueue(new MockResponse().setBody(body));
        object.put(PROPERTY_1, "3");
        object.save();

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
        object.save();

        server.shutdown();
    }

}
