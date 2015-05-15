package com.appstax;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;
import org.junit.Before;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public abstract class AppstaxTest {

    public static final String APP_KEY_1 = "YourAppKey";
    public static final String APP_KEY_2 = "SomeAppKey";

    public static final String COLLECTION_1 = "MyCollection";
    public static final String COLLECTION_2 = "BlankCollection";

    public static final String PROPERTY_1 = "property1";
    public static final String PROPERTY_2 = "property2";
    public static final String PROPERTY_3 = "property3";

    @Before
    public void before() {
        Appstax.setAppKey(APP_KEY_1);
    }

    public MockWebServer createMockWebServer() throws IOException {
        MockWebServer mock = new MockWebServer();
        mock.start();
        Appstax.setApiUrl(mock.getUrl("/").toString());
        return mock;
    }

    public String getResource(String path) throws IOException {
        String file = getClass().getResource(path).getFile();
        byte[] encoded = Files.readAllBytes(Paths.get(file));
        return new String(encoded, StandardCharsets.UTF_8);
    }

    public AppstaxObject getObject(MockWebServer server) throws Exception {
        String body = getResource("find-object-success.json");
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
