package com.appstax;

import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import static org.junit.Assert.assertEquals;

public class AxRefreshTest extends AxTest {

    @org.junit.Test
    public void shouldRefreshObject() throws Exception {
        MockWebServer server = createMockWebServer();
        enqueue(1, server, 200, getResource("find-object-success.json"));

        AxObject object = getObject(server);
        object.put("title", "unsaved");
        Ax.refresh(object);

        RecordedRequest req = server.takeRequest();
        assertEquals("GET", req.getMethod());

        assertEquals("123", object.getId());
        assertEquals("1", object.get("title"));
        assertEquals("2", object.get("contents"));

        server.shutdown();
    }

}
