package com.appstax;

import com.squareup.okhttp.mockwebserver.RecordedRequest;
import org.junit.Test;

import static org.junit.Assert.*;

public class AxSaveTest extends AxTest {

    @Test
    public void save() throws Exception {
        enqueue(1, 200, getResource("save-object-success.json"));

        AxObject object = ax.object(COLLECTION_1);
        assertNull(object.getId());
        object.put(PROPERTY_1, "1");

        ax.save(object);
        assertNotNull(object.getId());
        assertEquals("1", object.get(PROPERTY_1));

        RecordedRequest req = server.takeRequest();
        assertEquals("POST", req.getMethod());
        assertEquals("/objects/" + COLLECTION_1, req.getPath());
        assertEquals(APP_KEY, req.getHeader("x-appstax-appkey"));
    }

    @Test(expected=AxException.class)
    public void saveError() throws Exception {
        enqueue(1, 400, getResource("save-object-error.json"));
        AxObject object = ax.object(COLLECTION_1);
        ax.save(object);
    }

    @Test
    public void update() throws Exception {
        enqueue(1, 200, getResource("save-object-success.json"));

        AxObject object = getObject();
        object.put(PROPERTY_1, "3");
        ax.save(object);

        RecordedRequest req = server.takeRequest();
        assertEquals(object.getId(), "123");
        assertEquals(object.get(PROPERTY_1), "3");
        assertEquals("PUT", req.getMethod());
        assertEquals("/objects/" + COLLECTION_1 + "/" + object.getId(), req.getPath());
    }

    @Test(expected=AxException.class)
    public void updateError() throws Exception {
        enqueue(1, 400, getResource("save-object-error.json"));
        AxObject object = getObject();
        object.put(PROPERTY_1, "3");
        ax.save(object);
    }

}
