package com.appstax;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AppstaxRemoveTest extends AppstaxTest {

    @Test
    public void testRemoveOneSuccess() throws Exception {
        MockWebServer server = createMockWebServer();
        AppstaxObject object = getObject(server);

        server.enqueue(new MockResponse().setBody(""));
        assertEquals("123", object.getId());
        Appstax.remove(object);

        assertEquals(null, object.getId());
        assertEquals(null, object.get("title"));

        RecordedRequest req = server.takeRequest();
        assertEquals("DELETE", req.getMethod());
        assertEquals("/objects/" + COLLECTION_1 + "/123", req.getPath());

        server.shutdown();
    }

    @Test(expected=AppstaxException.class)
    public void testRemoveOneError() throws Exception {
        MockWebServer server = createMockWebServer();
        String body = getResource("remove-object-error.json");
        server.enqueue(new MockResponse().setBody(body).setResponseCode(404));

        AppstaxObject object = new AppstaxObject(COLLECTION_1);
        Appstax.remove(object);

        server.shutdown();
    }

}
