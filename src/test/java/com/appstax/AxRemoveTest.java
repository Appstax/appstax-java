package com.appstax;

import com.squareup.okhttp.mockwebserver.RecordedRequest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AxRemoveTest extends AxTest {

    @Test
    public void remove() throws Exception {
        AxObject object = getObject();
        assertEquals("123", object.getId());

        enqueue(1, 200, "");
        Ax.remove(object);
        assertEquals(null, object.getId());
        assertEquals(null, object.get("title"));

        RecordedRequest req = server.takeRequest();
        assertEquals("DELETE", req.getMethod());
        assertEquals("/objects/" + COLLECTION_1 + "/123", req.getPath());
    }

    @Test(expected=AxException.class)
    public void unsaved() throws Exception {
        enqueue(1, 404, getResource("remove-object-error.json"));
        AxObject object = new AxObject(COLLECTION_1);
        Ax.remove(object);
    }

}
