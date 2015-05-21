package com.appstax;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class AppstaxPermissionsTest extends AppstaxTest {

    @Test
    public void permitPublicSuccess() throws Exception {
        MockWebServer server = createMockWebServer();
        AppstaxObject object = new AppstaxObject(COLLECTION_2);

        object.grant(new ArrayList<String>() {{
            add("read");
            add("update");
        }});

        object.revoke(new ArrayList<String>(){{
            add("delete");
        }});

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

    @Test
    public void permitUserSuccess() throws Exception {
        MockWebServer server = createMockWebServer();
        AppstaxObject object = new AppstaxObject(COLLECTION_2);

        object.grant("foo", new ArrayList<String>() {{
            add("update");
        }});

        object.revoke("bar", new ArrayList<String>(){{
            add("read");
            add("delete");
        }});

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
