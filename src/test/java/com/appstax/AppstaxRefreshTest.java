package com.appstax;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AppstaxRefreshTest extends AppstaxTest {

    @Test
    public void testFindOneSuccess() throws Exception {
        MockWebServer server = createMockWebServer();
        AppstaxObject object = getObject(server);

        String body = getResource("find-object-success.json");
        server.enqueue(new MockResponse().setBody(body));
        Appstax.refresh(object);

        RecordedRequest req = server.takeRequest();
        assertEquals("GET", req.getMethod());

        assertEquals("123", object.getId());
        assertEquals("1", object.get("title"));
        assertEquals("2", object.get("contents"));

        server.shutdown();
    }

}
