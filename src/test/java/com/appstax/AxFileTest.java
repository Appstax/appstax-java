package com.appstax;

import com.squareup.okhttp.mockwebserver.RecordedRequest;
import org.junit.Test;

import static org.junit.Assert.*;

public class AxFileTest extends AxTest {

    @Test
    public void saveOne() throws Exception {
        enqueue(1, 200, getResource("save-object-success.json"));
        enqueue(1, 200, "");

        String filename = "file-example-image.jpg";
        AxObject object = new AxObject(COLLECTION_2);
        AxFile file = new AxFile(filename, getResource(filename).getBytes());
        object.put("image", file);
        assertNull(object.getId());
        assertNull(object.getFile("image").getUrl());

        Ax.save(object);
        assertNotNull(object.getId());
        assertNotNull(object.getFile("image").getUrl());
        assertTrue(object.getFile("image").getUrl().startsWith("http"));

        RecordedRequest req1 = server.takeRequest();
        String body = "{\"image\":{\"sysDatatype\":\"file\",\"filename\":\"file-example-image.jpg\"}}";
        assertEquals("POST", req1.getMethod());
        assertEquals(body, req1.getBody().readUtf8());

        RecordedRequest req2 = server.takeRequest();
        String multipart = req2.getBody().readUtf8();
        assertEquals("PUT", req2.getMethod());
        assertTrue(req2.getPath().contains("/files/"));
        assertTrue(req2.getHeader("Content-Type").startsWith("multipart"));
        assertTrue(multipart.contains("filedata"));
        assertTrue(multipart.contains("octet-stream"));
    }

    @Test
    public void saveMany() throws Exception {
        enqueue(1, 200, getResource("save-object-success.json"));
        enqueue(3, 200, "");

        String filename = "file-example-image.jpg";
        AxObject object = new AxObject(COLLECTION_2);

        object.put("image1", new AxFile("file-example-image-1.jpg", getResource(filename).getBytes()));
        object.put("image2", new AxFile("file-example-image-2.jpg", getResource(filename).getBytes()));
        object.put("image3", new AxFile("file-example-image-3.jpg", getResource(filename).getBytes()));

        Ax.save(object);

        RecordedRequest req1 = server.takeRequest();
        RecordedRequest req2 = server.takeRequest();
        RecordedRequest req3 = server.takeRequest();
        RecordedRequest req4 = server.takeRequest();

        assertEquals("POST", req1.getMethod());
        assertEquals("PUT", req2.getMethod());
        assertEquals("PUT", req3.getMethod());
        assertEquals("PUT", req4.getMethod());
    }

    @Test
    public void uploadOnce() throws Exception {
        enqueue(3, 200, getResource("save-object-success.json"));

        String filename = "file-example-image.jpg";
        AxObject object = new AxObject(COLLECTION_2);
        AxFile file = new AxFile(filename, getResource(filename).getBytes());
        object.put("image", file);

        Ax.save(object);
        Ax.save(object);

        assertEquals(3, server.getRequestCount());
    }

    @Test
    public void getFile() throws Exception {
        enqueue(1, 200, getResource("find-file-success.json"));

        String name = "file-example-image.jpg";
        AxObject object = Ax.find(COLLECTION_1, "123");
        AxFile file = object.getFile("file");
        assertNotNull(file);

        RecordedRequest req = server.takeRequest();
        assertEquals("GET", req.getMethod());
        assertEquals(name, file.getName());
        assertNull(file.getData());

        String exp = server.getUrl("/") + "files/" + object.getCollection() + "/" + object.getId() + "/file/" + name;
        String act = file.getUrl();
        assertEquals(exp, act);

        enqueue(1, 200, getResource("file-example-image.jpg"));
        file.load();
        assertNotNull(file.getData());

        req = server.takeRequest();
        assertEquals("GET", req.getMethod());
        assertEquals("http://" + req.getHeaders().get("Host"), server.getUrl("").toString());
        assertEquals(Ax.getAppKey(), req.getHeader("x-appstax-appkey"));
    }

    @Test(expected=AxException.class)
    public void getFileError() throws Exception {
        enqueue(1, 200, getResource("find-file-success.json"));
        enqueue(1, 400, getResource("find-object-error.json"));

        AxObject object = Ax.find(COLLECTION_1, "123");
        AxFile file = object.getFile("file");
        assertNull(file.getData());

        file.load();
    }

}
