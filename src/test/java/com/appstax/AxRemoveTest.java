package com.appstax;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import static org.junit.Assert.assertEquals;

public class AxRemoveTest extends AxTest {

    @org.junit.Test
    public void shouldRemoveObject() throws Exception {
        MockWebServer server = createMockWebServer();

        AxObject object = getObject(server);
        assertEquals("123", object.getId());

        enqueue(1, server, 200, "");
        Ax.remove(object);
        assertEquals(null, object.getId());
        assertEquals(null, object.get("title"));

        RecordedRequest req = server.takeRequest();
        assertEquals("DELETE", req.getMethod());
        assertEquals("/objects/" + COLLECTION_1 + "/123", req.getPath());

        server.shutdown();
    }

    @org.junit.Test(expected=AxException.class)
    public void shouldThrowOnRemoveUnsaved() throws Exception {
        MockWebServer server = createMockWebServer();
        enqueue(1, server, 404, getResource("remove-object-error.json"));

        AxObject object = new AxObject(COLLECTION_1);
        Ax.remove(object);

        server.shutdown();
    }

}
