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

        server.enqueue(new MockResponse().setBody(""));
        assertEquals("123", object.getId());
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
        String body = getResource("remove-object-error.json");
        server.enqueue(new MockResponse().setBody(body).setResponseCode(404));

        AxObject object = new AxObject(COLLECTION_1);
        Ax.remove(object);

        server.shutdown();
    }

}
