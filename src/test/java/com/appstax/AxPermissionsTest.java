package com.appstax;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import static org.junit.Assert.assertEquals;

public class AxPermissionsTest extends AxTest {

    @org.junit.Test
    public void shouldSavePublicAccess() throws Exception {
        MockWebServer server = createMockWebServer();
        AxObject object = new AxObject(COLLECTION_2);

        object.grantPublic("read", "update");
        object.revokePublic("delete");

        String body = getResource("save-object-success.json");
        server.enqueue(new MockResponse().setBody(body));
        server.enqueue(new MockResponse().setBody(""));
        object.save();

        RecordedRequest req = server.takeRequest();
        assertEquals("POST", req.getMethod());
        assertEquals("/objects/BlankCollection", req.getPath());

        req = server.takeRequest();
        String p1 = "{\"grants\":[{\"username\":\"*\",\"permissions\":[\"read\",\"update\"]}],";
        String p2 = "\"revokes\":[{\"username\":\"*\",\"permissions\":[\"delete\"]}]}";
        assertEquals("POST", req.getMethod());
        assertEquals("/permissions", req.getPath());
        assertEquals(p1 + p2, req.getBody().readUtf8());

        server.shutdown();
    }

    @org.junit.Test
    public void shouldSaveUserAccess() throws Exception {
        MockWebServer server = createMockWebServer();
        AxObject object = new AxObject(COLLECTION_2);

        object.grant("foo", "update");
        object.revoke("bar", "read", "delete");

        String body = getResource("save-object-success.json");
        server.enqueue(new MockResponse().setBody(body));
        server.enqueue(new MockResponse().setBody(""));
        object.save();

        RecordedRequest req = server.takeRequest();
        assertEquals("POST", req.getMethod());
        assertEquals("/objects/BlankCollection", req.getPath());

        req = server.takeRequest();
        String p1 = "{\"grants\":[{\"username\":\"foo\",\"permissions\":[\"update\"]}],";
        String p2 = "\"revokes\":[{\"username\":\"bar\",\"permissions\":[\"read\",\"delete\"]}]}";
        assertEquals("POST", req.getMethod());
        assertEquals("/permissions", req.getPath());
        assertEquals(p1 + p2, req.getBody().readUtf8());

        server.shutdown();
    }
}
