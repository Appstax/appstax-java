package com.appstax;

import com.squareup.okhttp.mockwebserver.RecordedRequest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AxPermissionsTest extends AxTest {

    @Test
    public void publicAccess() throws Exception {
        enqueue(2, 200, getResource("save-object-success.json"));

        AxObject object = ax.object(COLLECTION_2);
        object.grantPublic("read", "update");
        object.revokePublic("delete");
        object.save();

        RecordedRequest req1 = server.takeRequest();
        assertEquals("POST", req1.getMethod());
        assertEquals("/objects/BlankCollection", req1.getPath());

        RecordedRequest req2 = server.takeRequest();
        String p1 = "{\"grants\":[{\"username\":\"*\",\"permissions\":[\"read\",\"update\"]}],";
        String p2 = "\"revokes\":[{\"username\":\"*\",\"permissions\":[\"delete\"]}]}";
        assertEquals("POST", req2.getMethod());
        assertEquals("/permissions", req2.getPath());
        assertEquals(p1 + p2, req2.getBody().readUtf8());
    }

    @Test
    public void privateAccess() throws Exception {
        enqueue(2, 200, getResource("save-object-success.json"));

        AxObject object = ax.object(COLLECTION_2);
        object.grant("foo", "update");
        object.revoke("bar", "read", "delete");
        object.save();

        RecordedRequest req1 = server.takeRequest();
        assertEquals("POST", req1.getMethod());
        assertEquals("/objects/BlankCollection", req1.getPath());

        RecordedRequest req2 = server.takeRequest();
        String p1 = "{\"grants\":[{\"username\":\"foo\",\"permissions\":[\"update\"]}],";
        String p2 = "\"revokes\":[{\"username\":\"bar\",\"permissions\":[\"read\",\"delete\"]}]}";
        assertEquals("POST", req2.getMethod());
        assertEquals("/permissions", req2.getPath());
        assertEquals(p1 + p2, req2.getBody().readUtf8());
    }

}
