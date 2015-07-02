package com.appstax;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.Timeout;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public abstract class AxTest {

    public static final String APP_KEY_1 = "YourAppKey";
    public static final String APP_KEY_2 = "SomeAppKey";

    public static final String COLLECTION_1 = "MyCollection";
    public static final String COLLECTION_2 = "BlankCollection";

    public static final String PROPERTY_1 = "property1";
    public static final String PROPERTY_2 = "property2";
    public static final String PROPERTY_3 = "property3";

    protected MockWebServer server;
    protected Ax ax;

    @Rule
    public Timeout globalTimeout = new Timeout(1000);

    @Before
    public void before() throws Exception {
        server = new MockWebServer();
        server.start();
        ax = new Ax(APP_KEY_1, server.getUrl("/").toString());
    }

    @After
    public void after() throws Exception {
        server.shutdown();
    }

    public String getResource(String path) throws IOException {
        String file = getClass().getResource(path).getFile();
        byte[] encoded = Files.readAllBytes(Paths.get(file));
        return new String(encoded, StandardCharsets.UTF_8);
    }

    public AxObject getObject() throws Exception {
        String json = getResource("find-object-success.json");
        JSONObject props = new JSONObject(json);
        return ax.object(COLLECTION_1, props);
    }

    public void enqueue(int times, int status, String body) {
        for (int i = 0; i < times; i++) {
            server.enqueue(new MockResponse().setBody(body).setResponseCode(status));
        }
    }

}
