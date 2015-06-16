package com.appstax;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import static org.junit.Assert.assertTrue;

public class AxRelationsTest extends AxTest {

    @org.junit.Test
    public void setOneRelation() throws Exception {
        MockWebServer server = createMockWebServer();
        AxObject object = getObject(server);

        object.createRelation("messages", "1");
        String body = getResource("save-object-success.json");
        server.enqueue(new MockResponse().setBody(body));
        object.save();

        RecordedRequest req = server.takeRequest();
        String rel = "\"messages\":{\"sysRelationChanges\":{\"additions\":[\"1\"],\"removals\":[]}},";
        assertTrue(req.getBody().readUtf8().contains(rel));

        server.shutdown();
    }

    @org.junit.Test
    public void setManyRelations() throws Exception {
        MockWebServer server = createMockWebServer();
        AxObject object = getObject(server);

        object.createRelation("messages", "1", "2");
        object.createRelation("messages", "3");
        object.removeRelation("messages", "4");
        object.removeRelation("messages", "5", "6");
        object.createRelation("comments", "7", "8", "9");

        String body = getResource("save-object-success.json");
        server.enqueue(new MockResponse().setBody(body));
        object.save();

        RecordedRequest req = server.takeRequest();
        String resp = req.getBody().readUtf8();
        String rel1 = "\"messages\":{\"sysRelationChanges\":{\"additions\":[\"1\",\"2\",\"3\"],\"removals\":[\"4\",\"5\",\"6\"]}},";
        String rel2 = "\"comments\":{\"sysRelationChanges\":{\"additions\":[\"7\",\"8\",\"9\"],\"removals\":[]}},";

        assertTrue(resp.contains(rel1));
        assertTrue(resp.contains(rel2));

        server.shutdown();
    }

}
