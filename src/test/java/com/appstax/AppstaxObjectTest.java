package com.appstax;

import com.appstax.exceptions.AppstaxRequestException;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class AppstaxObjectTest {

    private static final String APP_KEY_1 = "YourAppKey";
    private static final String APP_KEY_2 = "SomeAppKey";

    private static final String COLLECTION_1 = "MyCollection";
    private static final String PROPERTY_1 = "property1";
    private static final String PROPERTY_2 = "property2";
    private static final String PROPERTY_3 = "property3";

    @Before
    public void before() {
        Appstax.setAppKey(APP_KEY_1);
    }

    @Test
    public void testObjectCollection() {
        AppstaxObject object = new AppstaxObject(COLLECTION_1);
        assertEquals(COLLECTION_1, object.getCollection());
    }

    @Test
    public void testObjectProperties() {
        AppstaxObject object = new AppstaxObject(COLLECTION_1);
        object.put(PROPERTY_1, 1);
        object.put(PROPERTY_2, 1.1);
        object.put(PROPERTY_3, "1");
        assertEquals(1, object.get(PROPERTY_1));
        assertEquals(1.1, object.get(PROPERTY_2));
        assertEquals("1", object.get(PROPERTY_3));
    }

    @Test(expected=AppstaxRequestException.class)
    public void testObjectSaveError() throws Exception {
        MockWebServer server = createMockWebServer();
        server.enqueue(new MockResponse().setBody(getResource("save-object-error.json")).setResponseCode(400));
        AppstaxObject object = new AppstaxObject(COLLECTION_1);
        object.save();
        server.shutdown();
    }

    @Test
    public void testObjectSaveSuccess() throws Exception {
        MockWebServer server = createMockWebServer();
        server.enqueue(new MockResponse().setBody(getResource("save-object-success.json")));

        AppstaxObject object = new AppstaxObject(COLLECTION_1);
        assertNull(object.getId());
        object.put(PROPERTY_1, "1");

        object.save();
        assertNotNull(object.getId());
        assertEquals("1", object.get(PROPERTY_1));

        RecordedRequest req = server.takeRequest();
        assertEquals("POST", req.getMethod());
        assertEquals("/objects/" + COLLECTION_1, req.getPath());
        assertEquals(APP_KEY_1, req.getHeader("x-appstax-appkey"));

        server.shutdown();
    }

    @Test
    public void testObjectFindSuccess() throws Exception {
        MockWebServer server = createMockWebServer();
        server.enqueue(new MockResponse().setBody(getResource("find-object-success.json")));

        AppstaxObject object = AppstaxObject.find(COLLECTION_1, "123");
        assertEquals("123", object.getId());
        assertEquals("1", object.get("title"));
        assertEquals("2", object.get("contents"));

        RecordedRequest req = server.takeRequest();
        assertEquals("GET", req.getMethod());
        assertEquals("", req.getBody().readUtf8());
        assertEquals("/objects/" + COLLECTION_1 + "/" + object.getId(), req.getPath());

        server.shutdown();
    }

    @Test
    public void testObjectUpdateSuccess() throws Exception {
        MockWebServer server = createMockWebServer();
        server.enqueue(new MockResponse().setBody(getResource("find-object-success.json")));
        AppstaxObject object = AppstaxObject.find(COLLECTION_1, "123");

        RecordedRequest req1 = server.takeRequest();
        assertEquals("123", object.getId());
        assertEquals("GET", req1.getMethod());

        server.enqueue(new MockResponse().setBody(getResource("save-object-success.json")));
        object.put(PROPERTY_1, "3");
        object.save();

        RecordedRequest req2 = server.takeRequest();
        assertEquals(object.getId(), "123");
        assertEquals(object.get(PROPERTY_1), "3");
        assertEquals("PUT", req2.getMethod());
        assertEquals("/objects/" + COLLECTION_1 + "/" + object.getId(), req2.getPath());

        server.shutdown();
    }

    @Test(expected=AppstaxRequestException.class)
    public void testObjectFindError() throws Exception {
        MockWebServer server = createMockWebServer();
        server.enqueue(new MockResponse().setBody(getResource("find-object-error.json")).setResponseCode(404));
        AppstaxObject object = AppstaxObject.find(COLLECTION_1, "404");
        server.shutdown();
    }

    private MockWebServer createMockWebServer() throws IOException {
        MockWebServer mock = new MockWebServer();
        mock.start();
        Appstax.setApiUrl(mock.getUrl("/").toString());
        return mock;
    }

    private String getResource(String path) throws IOException {
        String file = getClass().getResource(path).getFile();
        byte[] encoded = Files.readAllBytes(Paths.get(file));
        return new String(encoded, StandardCharsets.UTF_8);
    }

}