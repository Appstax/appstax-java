package com.appstax;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AppstaxObjectFindTest extends AppstaxTest {

    @Test
    public void testFindSuccess() throws Exception {
        MockWebServer server = createMockWebServer();
        AppstaxObject object = getObject(server);

        assertEquals("123", object.getId());
        assertEquals("1", object.get("title"));
        assertEquals("2", object.get("contents"));

        server.shutdown();
    }

    @Test(expected=AppstaxException.class)
    public void testFindError() throws Exception {
        MockWebServer server = createMockWebServer();
        String body = getResource("find-object-error.json");
        server.enqueue(new MockResponse().setBody(body).setResponseCode(404));

        AppstaxObject object = AppstaxObject.find(COLLECTION_1, "404");
        server.shutdown();
    }

}
