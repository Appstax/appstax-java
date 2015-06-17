package com.appstax;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AxRelationsTest extends AxTest {

    @org.junit.Test
    public void shouldGetRelations() throws Exception {
        MockWebServer server = createMockWebServer();
        String body = getResource("find-relation-success.json");
        server.enqueue(new MockResponse().setBody(body));
        AxObject object = Ax.find(COLLECTION_1, "0lxenePip1Z5zb");
        assertEquals("0lxenePip1Z5zb", object.getId());
    }

    @org.junit.Test
    public void shouldSaveRelations() throws Exception {
        MockWebServer server = createMockWebServer();

        AxObject object1 = getObject(server);
        AxObject object2 = getObject(server);
        AxObject object3 = getObject(server);
        AxObject object4 = getObject(server);

        String body = getResource("save-object-success.json");
        server.enqueue(new MockResponse().setBody(body));

        object1
                .createRelation("messages", object2, object3)
                .removeRelation("messages", object4)
                .save();

        RecordedRequest req = server.takeRequest();
        String rel = "\"messages\":{\"sysRelationChanges\":{\"additions\":[\"123\",\"123\"],\"removals\":[\"123\"]}}";
        assertTrue(req.getBody().readUtf8().contains(rel));

        server.shutdown();
    }

    @org.junit.Test(expected=AxException.class)
    public void shouldThrowOnUnsavedRelation() throws Exception {
        MockWebServer server = createMockWebServer();

        AxObject object1 = getObject(server);
        AxObject object2 = new AxObject(COLLECTION_1);

        String body = getResource("save-object-success.json");
        server.enqueue(new MockResponse().setBody(body));

        object1.createRelation("comments", object2).save();
        server.shutdown();
    }

}
