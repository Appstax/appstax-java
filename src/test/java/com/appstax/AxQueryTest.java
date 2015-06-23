package com.appstax;

import com.squareup.okhttp.mockwebserver.RecordedRequest;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class AxQueryTest extends AxTest {

    @Test
    public void findOne() throws Exception {
        enqueue(1, 200, getResource("find-object-success.json"));
        AxObject object = Ax.find(COLLECTION_1, "123");
        RecordedRequest req = server.takeRequest();

        assertEquals("GET", req.getMethod());
        assertEquals("", req.getBody().readUtf8());
        assertEquals("/objects/" + COLLECTION_1 + "/" + object.getId(), req.getPath());
        assertEquals("123", object.getId());
        assertEquals("1", object.get("title"));
        assertEquals("2", object.get("contents"));
    }

    @Test(expected=AxException.class)
    public void findError() throws Exception {
        enqueue(1, 400, getResource("find-object-error.json"));
        AxObject object = Ax.find(COLLECTION_1, "404");
    }

    @Test
    public void findMany() throws Exception {
        enqueue(1, 200, getResource("find-objects-success.json"));

        List<AxObject> objects = Ax.find(COLLECTION_1);
        assertEquals(3, objects.size());
        assertEquals("1", objects.get(0).get("title"));
        assertEquals("3", objects.get(2).get("title"));
    }

    @Test
    public void expand() throws Exception {
        enqueue(1, 200, getResource("find-objects-success.json"));

        List<AxObject> objects = Ax.find(COLLECTION_1, 2);
        RecordedRequest req = server.takeRequest();
        assertEquals("/objects/" + COLLECTION_1 + "?expanddepth=2", req.getPath());
    }

    @Test
    public void filterString() throws Exception {
        enqueue(1, 200, getResource("find-objects-success.json"));

        String filter = "Age > 42 and name like 'Alex%'";
        List<AxObject> objects = Ax.filter(COLLECTION_1, filter);
        assertEquals(3, objects.size());

        RecordedRequest req = server.takeRequest();
        assertEquals("GET", req.getMethod());

        String sub = req.getPath().substring(req.getPath().indexOf("=") + 1);
        assertEquals(AxPaths.encode(filter), sub);
    }

    @Test
    public void filterProps() throws Exception {
        enqueue(1, 200, getResource("find-objects-success.json"));

        HashMap properties = new HashMap<String, String>();
        properties.put("foo", "b r");

        List<AxObject> objects = Ax.filter(COLLECTION_1, properties);
        assertEquals(3, objects.size());

        RecordedRequest req = server.takeRequest();
        assertEquals("GET", req.getMethod());

        String exp = "filter=foo%3D%27b%20r%27";
        String act = req.getPath().substring(req.getPath().indexOf("filter"));
        assertEquals(exp, act);
    }

}
