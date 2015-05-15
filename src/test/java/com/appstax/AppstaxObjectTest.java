package com.appstax;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;
import org.junit.Before;
import org.junit.Test;

import static com.appstax.AppstaxTestHelpers.createMockWebServer;
import static com.appstax.AppstaxTestHelpers.getResource;
import static org.junit.Assert.*;

public class AppstaxObjectTest {

    private static final String COLLECTION_1 = "MyCollection";
    private static final String PROPERTY_1 = "property1";
    private static final String PROPERTY_2 = "property2";
    private static final String PROPERTY_3 = "property3";

    @Before
    public void before() {
        Appstax.setAppKey(AppstaxTest.APP_KEY_1);
    }

    @Test
    public void testCollection() {
        AppstaxObject object = new AppstaxObject(COLLECTION_1);
        assertEquals(COLLECTION_1, object.getCollection());
    }

    @Test
    public void testProperties() {
        AppstaxObject object = new AppstaxObject(COLLECTION_1);
        object.put(PROPERTY_1, 1);
        object.put(PROPERTY_2, 1.1);
        object.put(PROPERTY_3, "1");
        assertEquals(1, object.get(PROPERTY_1));
        assertEquals(1.1, object.get(PROPERTY_2));
        assertEquals("1", object.get(PROPERTY_3));
    }

    @Test
    public void testSaveSuccess() throws Exception {
        MockWebServer server = createMockWebServer();
        String body = getResource(getClass(), "save-object-success.json");
        server.enqueue(new MockResponse().setBody(body));

        AppstaxObject object = new AppstaxObject(COLLECTION_1);
        assertNull(object.getId());
        object.put(PROPERTY_1, "1");

        object.save();
        assertNotNull(object.getId());
        assertEquals("1", object.get(PROPERTY_1));

        RecordedRequest req = server.takeRequest();
        assertEquals("POST", req.getMethod());
        assertEquals("/objects/" + COLLECTION_1, req.getPath());
        assertEquals(AppstaxTest.APP_KEY_1, req.getHeader("x-appstax-appkey"));

        server.shutdown();
    }

    @Test(expected=AppstaxException.class)
    public void testSaveError() throws Exception {
        MockWebServer server = createMockWebServer();
        String body = getResource(getClass(), "save-object-error.json");
        server.enqueue(new MockResponse().setBody(body).setResponseCode(400));

        AppstaxObject object = new AppstaxObject(COLLECTION_1);
        object.save();
        server.shutdown();
    }

    @Test
    public void testFindSuccess() throws Exception {
        MockWebServer server = createMockWebServer();
        AppstaxObject object = findObject(server);

        assertEquals("123", object.getId());
        assertEquals("1", object.get("title"));
        assertEquals("2", object.get("contents"));

        server.shutdown();
    }

    @Test(expected=AppstaxException.class)
    public void testFindError() throws Exception {
        MockWebServer server = createMockWebServer();
        String body = getResource(getClass(), "find-object-error.json");
        server.enqueue(new MockResponse().setBody(body).setResponseCode(404));

        AppstaxObject object = AppstaxObject.find(COLLECTION_1, "404");
        server.shutdown();
    }

    @Test
    public void testUpdateSuccess() throws Exception {
        MockWebServer server = createMockWebServer();
        AppstaxObject object = findObject(server);

        String body = getResource(getClass(), "save-object-success.json");
        server.enqueue(new MockResponse().setBody(body));
        object.put(PROPERTY_1, "3");
        object.save();

        RecordedRequest req = server.takeRequest();
        assertEquals(object.getId(), "123");
        assertEquals(object.get(PROPERTY_1), "3");
        assertEquals("PUT", req.getMethod());
        assertEquals("/objects/" + COLLECTION_1 + "/" + object.getId(), req.getPath());

        server.shutdown();
    }

    @Test(expected=AppstaxException.class)
    public void testUpdateError() throws Exception {
        MockWebServer server = createMockWebServer();
        AppstaxObject object = findObject(server);

        String body = getResource(getClass(), "save-object-error.json");
        server.enqueue(new MockResponse().setBody(body).setResponseCode(400));
        object.put(PROPERTY_1, "3");
        object.save();

        server.shutdown();
    }

    private AppstaxObject findObject(MockWebServer server) throws Exception {
        String body = getResource(getClass(), "find-object-success.json");
        server.enqueue(new MockResponse().setBody(body));
        AppstaxObject object = AppstaxObject.find(COLLECTION_1, "123");

        RecordedRequest req = server.takeRequest();
        assertEquals("GET", req.getMethod());
        assertEquals("", req.getBody().readUtf8());
        assertEquals("/objects/" + COLLECTION_1 + "/" + object.getId(), req.getPath());
        assertEquals("123", object.getId());

        return object;
    }

}