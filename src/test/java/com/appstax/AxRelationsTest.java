package com.appstax;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import static org.junit.Assert.*;

public class AxRelationsTest extends AxTest {

    @org.junit.Test
    public void shouldGetParsedRelations() throws Exception {
        MockWebServer server = createMockWebServer();
        String body = getResource("find-relation-success.json");
        server.enqueue(new MockResponse().setBody(body));
        AxObject object = Ax.find(COLLECTION_1, "0lxenePip1Z5zb");

        assertEquals("0lxenePip1Z5zb", object.getId());
        assertNull(object.get("messages"));

        assertNotNull(object.getAll("messages").get(0).getId());
        assertNotNull(object.getAll("messages").get(0).getOne("author").getId());
        assertNotNull(object.getAll("messages").get(0).getAll("comments").get(0).getId());
        assertNotNull(object.getAll("messages").get(0).getAll("comments").get(0).getOne("author").getId());

        server.shutdown();
    }

    @org.junit.Test
    public void shouldGetNewRelations() throws Exception {
        MockWebServer server = createMockWebServer();

        String body = getResource("save-object-success.json");
        server.enqueue(new MockResponse().setBody(body));
        server.enqueue(new MockResponse().setBody(body));
        server.enqueue(new MockResponse().setBody(body));

        AxObject object1 = new AxObject(COLLECTION_1);
        AxObject object2 = new AxObject(COLLECTION_2);
        AxObject object3 = new AxObject(COLLECTION_1);

        Ax.save(object1);
        Ax.save(object2);
        Ax.save(object3);

        String name = "foo";
        object3.put("name", name);
        object2.createRelation("author", object3);
        object1.createRelation("posts", object2);
        assertEquals(name, object1.getAll("posts").get(0).getOne("author").get("name"));

        server.shutdown();
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
            .createRelation("comments", object2, object2);
        Ax.save(object1);

        RecordedRequest req = server.takeRequest();
        String res = req.getBody().readUtf8();

        String rel1 = "\"messages\":{\"sysRelationChanges\":{\"additions\":[\"123\",\"123\"],\"removals\":[\"123\"]}}";
        String rel2 = "\"comments\":{\"sysRelationChanges\":{\"additions\":[\"123\",\"123\"]}}";
        assertTrue(res.contains(rel1));
        assertTrue(res.contains(rel2));

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

    @org.junit.Test
    public void shouldSaveAllRelations() throws Exception {
        MockWebServer server = createMockWebServer();

        AxObject object1 = getObject(server);
        AxObject object2 = getObject(server);
        AxObject object3 = getObject(server);
        AxObject object4 = getObject(server);

        String body = getResource("save-object-success.json");
        server.enqueue(new MockResponse().setBody(body));
        server.enqueue(new MockResponse().setBody(body));
        server.enqueue(new MockResponse().setBody(body));
        server.enqueue(new MockResponse().setBody(body));
        server.enqueue(new MockResponse().setBody(body));

        object3.createRelation("rel4", object4);
        object2.createRelation("rel3", object3);
        object1.createRelation("rel2", object2);
        Ax.saveAll(object4);
        Ax.saveAll(object1);

        assertEquals(9, server.getRequestCount());

        server.shutdown();
    }

}
