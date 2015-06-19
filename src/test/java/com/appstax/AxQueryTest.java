package com.appstax;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class AxQueryTest extends AxTest {

    @org.junit.Test
    public void shouldFindOneObject() throws Exception {
        MockWebServer server = createMockWebServer();
        AxObject object = getObject(server);

        assertEquals("123", object.getId());
        assertEquals("1", object.get("title"));
        assertEquals("2", object.get("contents"));

        server.shutdown();
    }

    @org.junit.Test(expected=AxException.class)
    public void shouldThrowOnFindError() throws Exception {
        MockWebServer server = createMockWebServer();
        enqueue(1, server, 400, getResource("find-object-error.json"));
        AxObject object = Ax.find(COLLECTION_1, "404");
        server.shutdown();
    }

    @org.junit.Test
    public void shouldFindMultipleObjects() throws Exception {
        MockWebServer server = createMockWebServer();
        enqueue(1, server, 200, getResource("find-objects-success.json"));

        List<AxObject> objects = Ax.find(COLLECTION_1);
        assertEquals(3, objects.size());
        assertEquals("1", objects.get(0).get("title"));
        assertEquals("3", objects.get(2).get("title"));

        server.shutdown();
    }

    @org.junit.Test
    public void shouldAddExpansionProperty() throws Exception {
        MockWebServer server = createMockWebServer();
        enqueue(1, server, 200, getResource("find-objects-success.json"));

        List<AxObject> objects = Ax.find(COLLECTION_1, 2);
        RecordedRequest req = server.takeRequest();
        assertEquals("/objects/" + COLLECTION_1 + "?expanddepth=2", req.getPath());

        server.shutdown();
    }

    @org.junit.Test
    public void shouldSendFilterString() throws Exception {
        MockWebServer server = createMockWebServer();
        enqueue(1, server, 200, getResource("find-objects-success.json"));

        List<AxObject> objects = Ax.filter(COLLECTION_1, "Age > 42 and name like 'Alex%'");
        assertEquals(3, objects.size());

        RecordedRequest req = server.takeRequest();
        assertEquals("GET", req.getMethod());
        String expected = "filter=Age+%3E+42+and+name+like+%27Alex%25%27";
        String actual = req.getPath().substring(req.getPath().indexOf("filter"));
        assertEquals(expected, actual);

        server.shutdown();
    }

    @org.junit.Test
    public void shouldSendFilterProperties() throws Exception {
        MockWebServer server = createMockWebServer();
        enqueue(1, server, 200, getResource("find-objects-success.json"));

        HashMap properties = new HashMap<String, String>();
        properties.put("foo", "b r");

        List<AxObject> objects = Ax.filter(COLLECTION_1, properties);
        assertEquals(3, objects.size());

        RecordedRequest req = server.takeRequest();
        assertEquals("GET", req.getMethod());

        String expected = "filter=foo%3D%27b+r%27";
        String actual = req.getPath().substring(req.getPath().indexOf("filter"));
        assertEquals(expected, actual);

        server.shutdown();
    }

}
