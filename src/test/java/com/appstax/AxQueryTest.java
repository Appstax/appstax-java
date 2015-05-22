package com.appstax;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class AxQueryTest extends AxTest {

    @org.junit.Test
    public void testFindOneSuccess() throws Exception {
        MockWebServer server = createMockWebServer();
        AxObject object = getObject(server);

        assertEquals("123", object.getId());
        assertEquals("1", object.get("title"));
        assertEquals("2", object.get("contents"));

        server.shutdown();
    }

    @org.junit.Test(expected=AxException.class)
    public void testFindOneError() throws Exception {
        MockWebServer server = createMockWebServer();
        String body = getResource("find-object-error.json");
        server.enqueue(new MockResponse().setBody(body).setResponseCode(404));

        AxObject object = Ax.find(COLLECTION_1, "404");
        server.shutdown();
    }

    @org.junit.Test
    public void testFindAllSuccess() throws Exception {
        MockWebServer server = createMockWebServer();
        String body = getResource("find-objects-success.json");
        server.enqueue(new MockResponse().setBody(body));

        List<AxObject> objects = Ax.find(COLLECTION_1);
        assertEquals(3, objects.size());
        assertEquals("1", objects.get(0).get("title"));
        assertEquals("3", objects.get(2).get("title"));

        server.shutdown();
    }

    @org.junit.Test
    public void testFilterString() throws Exception {
        MockWebServer server = createMockWebServer();
        String body = getResource("find-objects-success.json");
        server.enqueue(new MockResponse().setBody(body));

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
    public void testFilterProperties() throws Exception {
        MockWebServer server = createMockWebServer();
        String body = getResource("find-objects-success.json");
        server.enqueue(new MockResponse().setBody(body));

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
