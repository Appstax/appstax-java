package com.appstax;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class AppstaxObjectFindTest extends AppstaxTest {

    @Test
    public void testFindOneSuccess() throws Exception {
        MockWebServer server = createMockWebServer();
        AppstaxObject object = getObject(server);

        assertEquals("123", object.getId());
        assertEquals("1", object.get("title"));
        assertEquals("2", object.get("contents"));

        server.shutdown();
    }

    @Test(expected=AppstaxException.class)
    public void testFindOneError() throws Exception {
        MockWebServer server = createMockWebServer();
        String body = getResource("find-object-error.json");
        server.enqueue(new MockResponse().setBody(body).setResponseCode(404));

        AppstaxObject object = AppstaxObject.find(COLLECTION_1, "404");
        server.shutdown();
    }

    @Test
    public void testFindAllSuccess() throws Exception {
        MockWebServer server = createMockWebServer();
        String body = getResource("find-objects-success.json");
        server.enqueue(new MockResponse().setBody(body));

        List<AppstaxObject> objects = AppstaxObject.find(COLLECTION_1);
        assertEquals(3, objects.size());
        assertEquals("1", objects.get(0).get("title"));
        assertEquals("3", objects.get(2).get("title"));

        server.shutdown();
    }

}
