package com.appstax;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AppstaxFileTest extends AppstaxTest {

    @Test
    public void testFileSuccess() throws Exception {
        MockWebServer server = createMockWebServer();

        server.enqueue(new MockResponse().setBody(getResource("save-object-success.json")));
        server.enqueue(new MockResponse().setBody(""));

        String filename = "file-example-image.jpg";
        AppstaxObject object = new AppstaxObject(COLLECTION_2);
        AppstaxFile file = new AppstaxFile(filename, getResource(filename));
        object.put("image", file);
        Appstax.save(object);

        RecordedRequest req1 = server.takeRequest();
        String body = "{\"image\":{\"sysDatatype\":\"file\",\"filename\":\"file-example-image.jpg\"}}";
        assertEquals("POST", req1.getMethod());
        assertEquals(body, req1.getBody().readUtf8());

        RecordedRequest req2 = server.takeRequest();
        assertEquals("PUT", req2.getMethod());
        assertTrue(req2.getPath().indexOf("/files/") > -1);
        assertTrue(req2.getHeader("Content-Type").startsWith("multipart"));
        assertTrue(req2.getBody().readUtf8().indexOf("filedata") > -1);

        server.shutdown();
    }

    @Test
    public void testMultiFileSuccess() throws Exception {
        MockWebServer server = createMockWebServer();
        server.enqueue(new MockResponse().setBody(getResource("save-object-success.json")));
        server.enqueue(new MockResponse().setBody(""));
        server.enqueue(new MockResponse().setBody(""));
        server.enqueue(new MockResponse().setBody(""));

        String filename = "file-example-image.jpg";
        AppstaxObject object = new AppstaxObject(COLLECTION_2);

        object.put("image1", new AppstaxFile("file-example-image-1.jpg", getResource(filename)));
        object.put("image2", new AppstaxFile("file-example-image-2.jpg", getResource(filename)));
        object.put("image3", new AppstaxFile("file-example-image-3.jpg", getResource(filename)));

        Appstax.save(object);

        RecordedRequest req1 = server.takeRequest();
        RecordedRequest req2 = server.takeRequest();
        RecordedRequest req3 = server.takeRequest();
        RecordedRequest req4 = server.takeRequest();

        assertEquals("POST", req1.getMethod());
        assertEquals("PUT", req2.getMethod());
        assertEquals("PUT", req3.getMethod());
        assertEquals("PUT", req4.getMethod());

        server.shutdown();
    }
}
