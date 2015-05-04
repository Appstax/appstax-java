package com.appstax;

import com.appstax.exceptions.AppstaxRequestException;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class AppstaxTest {

    private static final String APP_KEY_1 = "YourAppKey";
    private static final String APP_KEY_2 = "SomeAppKey";

    private static final String COLLECTION_1 = "MyCollection";
    private static final String PROPERTY_1 = "property1";
    private static final String PROPERTY_2 = "property2";
    private static final String PROPERTY_3 = "property3";

    private static MockWebServer server;


    @BeforeClass
    public static void beforeClass() throws IOException {
        server = new MockWebServer();
        server.start();
    }

    @AfterClass
    public static void afterClass() throws IOException {
        server.shutdown();
    }

    @Before
    public void before() {
        Appstax.setAppKey(APP_KEY_1);
        Appstax.setApiUrl(server.getUrl("/").toString());
    }

    @Test
    public void testAppKey() {
        Appstax.setAppKey(APP_KEY_2);
        assertEquals(APP_KEY_2, Appstax.getAppKey());
    }

    @Test
    public void testApiUrlSlash() {
        Appstax.setApiUrl("1");
        assertEquals("1/", Appstax.getApiUrl());
        Appstax.setApiUrl("2/");
        assertEquals("2/", Appstax.getApiUrl());
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
        server.enqueue(new MockResponse().setBody(resource("save-object-error.json")).setResponseCode(400));
        AppstaxObject object = new AppstaxObject(COLLECTION_1);
        object.save();
    }

    @Test
    public void testObjectSaveSuccess() throws Exception {
        RecordedRequest request = server.takeRequest();
        server.enqueue(new MockResponse().setBody(resource("save-object-response.json")));

        AppstaxObject object = new AppstaxObject(COLLECTION_1);
        assertNull(object.getId());

        object.save();
        assertNotNull(object.getId());

        assertEquals(APP_KEY_1, request.getHeader("x-appstax-appkey"));
        assertEquals("/objects/" + COLLECTION_1, request.getPath());
        assertEquals("POST", request.getMethod());
    }

    @Test
    public void testObjectFindError() throws Exception {
        server.enqueue(new MockResponse().setBody(resource("find-object-error.json")));
        AppstaxObject object = AppstaxObject.find("404");
        assertNull(object);
    }

    private String resource(String path) throws IOException {
        String file = getClass().getResource(path).getFile();
        byte[] encoded = Files.readAllBytes(Paths.get(file));
        return new String(encoded, StandardCharsets.UTF_8);
    }

}