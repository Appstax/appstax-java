package com.appstax;

import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AxRefreshTest extends AxTest {

    @Test
    public void refresh() throws Exception {
        enqueue(1, 200, getResource("find-object-success.json"));

        AxObject object = getObject();
        object.put("title", "unsaved");
        Ax.refresh(object);

        RecordedRequest req = server.takeRequest();
        assertEquals("GET", req.getMethod());

        assertEquals("123", object.getId());
        assertEquals("1", object.get("title"));
        assertEquals("2", object.get("contents"));
    }

}
