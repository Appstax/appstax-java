package com.appstax;

import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class AxRelationsTest extends AxTest {

    @org.junit.Test
    public void shouldParseUnexpandedObject() throws Exception {
        MockWebServer server = createMockWebServer();
        enqueue(1, server, 200, getResource("relation-unexpanded-success.json"));
        AxObject object = Ax.find(COLLECTION_1, "123");

        List<String> expected = new ArrayList<>();
        List<String> actual = object.getStrings("messages");

        expected.add("KqXZPjCEyQKm");
        expected.add("DQXzz4BSAreWq");

        assertEquals(expected, actual);
        assertNull(object.getObjects("messages"));

        server.shutdown();
    }

    @org.junit.Test
    public void shouldParseExpandedObject() throws Exception {
        MockWebServer server = createMockWebServer();
        enqueue(1, server, 200, getResource("relation-expanded-one-success.json"));
        AxObject object = Ax.find(COLLECTION_1, "123", 10);

        assertNotNull(object.get("messages"));
        assertNotNull(object.getObjects("messages").get(0).getId());
        assertNotNull(object.getObjects("messages").get(0).getObject("author").getId());
        assertNotNull(object.getObjects("messages").get(0).getObjects("comments").get(0).getId());
        assertNotNull(object.getObjects("messages").get(0).getObjects("comments").get(0).getObject("author").getId());

        server.shutdown();
    }

    @org.junit.Test
    public void shouldParseUserRelation() throws Exception {
        MockWebServer server = createMockWebServer();
        enqueue(1, server, 200, getResource("relation-users-success.json"));
        List<AxObject> objects = Ax.find(COLLECTION_1, 1);

        assertEquals(2, objects.size());
        assertNotNull(objects.get(0).getFile("image").getUrl());
        assertEquals("GrAjO8Fa2zLz", objects.get(0).getStrings("user").get(0));
        assertEquals("GrAjO8Fa2zLz", objects.get(0).getObject("user").getId());
        assertEquals("GrAjO8Fa2zLz", objects.get(0).getObjects("user").get(0).getId());

        server.shutdown();
    }

    @org.junit.Test
    public void shouldParseExpandedCollection() throws Exception {
        MockWebServer server = createMockWebServer();
        enqueue(1, server, 200, getResource("relation-expanded-all-success.json"));
        List<AxObject> objects = Ax.find(COLLECTION_1, 10);

        assertEquals(1, objects.size());
        AxObject object = objects.get(0);

        assertNotNull(object.get("messages"));
        assertNotNull(object.getObjects("messages").get(0).getObjects("comments").get(0).getObject("author").getId());

        server.shutdown();
    }

    @org.junit.Test
    public void shouldIncludeNewRelations() throws Exception {
        MockWebServer server = createMockWebServer();
        enqueue(3, server, 200, getResource("save-object-success.json"));

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
        assertEquals(name, object1.getObjects("posts").get(0).getObject("author").get("name"));

        server.shutdown();
    }

    @org.junit.Test
    public void shouldSaveRelations() throws Exception {
        MockWebServer server = createMockWebServer();

        AxObject object1 = getObject(server);
        AxObject object2 = getObject(server);
        AxObject object3 = getObject(server);
        AxObject object4 = getObject(server);

        String rel1 = "\"messages\":{\"sysRelationChanges\":{\"additions\":[\"123\",\"123\"],\"removals\":[\"123\"]}}";
        String rel2 = "\"comments\":{\"sysRelationChanges\":{\"additions\":[\"123\",\"123\"]}}";
        enqueue(2, server, 200, getResource("save-object-success.json"));

        object1
            .createRelation("messages", object2, object3)
            .removeRelation("messages", object4)
            .createRelation("comments", object2, object2);

        Ax.save(object1);
        RecordedRequest req1 = server.takeRequest();
        String res1 = req1.getBody().readUtf8();
        assertTrue(res1.contains(rel1));
        assertTrue(res1.contains(rel2));

        Ax.save(object1);
        RecordedRequest req2 = server.takeRequest();
        String res2 = req2.getBody().readUtf8();
        assertFalse(res2.contains(rel1));
        assertFalse(res2.contains(rel2));

        server.shutdown();
    }

    @org.junit.Test(expected=AxException.class)
    public void shouldThrowOnUnsavedRelation() throws Exception {
        MockWebServer server = createMockWebServer();

        AxObject object1 = getObject(server);
        AxObject object2 = new AxObject(COLLECTION_1);

        enqueue(1, server, 200, getResource("save-object-success.json"));
        object1.createRelation("comments", object2).save();

        server.shutdown();
    }

    @org.junit.Test
    public void shouldSaveAllRelations() throws Exception {
        MockWebServer server = createMockWebServer();

        AxObject object1 = getObject(server);
        AxObject object2 = getObject(server);
        AxObject object3 = getObject(server);

        enqueue(4, server, 200, getResource("save-object-success.json"));
        object2.createRelation("rel3", object3);
        object1.createRelation("rel2", object2);
        Ax.saveAll(object3);
        Ax.saveAll(object1);

        assertEquals(7, server.getRequestCount());
        server.shutdown();
    }

    @org.junit.Test
    public void shouldHandleRelationToUser() throws Exception {
        MockWebServer server = createMockWebServer();
        AxObject object = getObject(server);

        enqueue(1, server, 200, getResource("save-object-success.json"));
        AxUser user = new AxUser("foo", "bar");
        user.put("1", "2");
        Ax.save(user);

        enqueue(1, server, 200, getResource("save-object-success.json"));
        object.createRelation("author", user);
        Ax.save(object);

        server.shutdown();
    }

    @org.junit.Test
    public void shouldHandleCyclicalRelations() throws Exception {
        MockWebServer server = createMockWebServer();
        String body = getResource("save-object-success.json");

        AxObject object1 = getObject(server); // 1 req
        AxObject object2 = getObject(server); // 1 req
        AxObject object3 = getObject(server); // 1 req
        AxObject object4 = getObject(server); // 1 req

        enqueue(4, server, 200, body);
        object1.createRelation("foo", object2, object3);
        object2.createRelation("bar", object3, object1);
        object3.createRelation("baz", object1, object2);
        object4.createRelation("baz", object1);

        Ax.save(object1); // 1 req
        Ax.save(object2); // 1 req
        Ax.save(object3); // 1 req
        Ax.save(object4); // 1 req

        enqueue(13, server, 200, body);
        Ax.saveAll(object1); // 3 reqs
        Ax.saveAll(object2); // 3 reqs
        Ax.saveAll(object3); // 3 reqs
        Ax.saveAll(object4); // 4 reqs

        assertEquals(4+4+13, server.getRequestCount());
        server.shutdown();
    }

}
