package com.appstax;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AppstaxDeleteTest extends AppstaxTest {

    @Test
    public void testDeleteOneSuccess() throws Exception {
        MockWebServer server = createMockWebServer();
        AppstaxObject object = getObject(server);

        String body = getResource("delete-object-success.json");
        server.enqueue(new MockResponse().setBody(body));

        assertEquals("123", object.getId());
        Appstax.delete(object);

        assertEquals(null, object.getId());
        assertEquals(null, object.get("title"));

        RecordedRequest req = server.takeRequest();
        assertEquals("DELETE", req.getMethod());
        assertEquals("/objects/" + COLLECTION_1 + "/123", req.getPath());

        server.shutdown();
    }

    @Test(expected=AppstaxException.class)
    public void testDeleteOneError() throws Exception {
        MockWebServer server = createMockWebServer();
        String body = getResource("find-object-error.json");
        server.enqueue(new MockResponse().setBody(body).setResponseCode(404));

        AppstaxObject object = Appstax.find(COLLECTION_1, "404");
        server.shutdown();
    }

}
