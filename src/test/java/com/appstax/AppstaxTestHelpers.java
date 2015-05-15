package com.appstax;

import com.squareup.okhttp.mockwebserver.MockWebServer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

abstract class AppstaxTestHelpers {

    public static MockWebServer createMockWebServer() throws IOException {
        MockWebServer mock = new MockWebServer();
        mock.start();
        Appstax.setApiUrl(mock.getUrl("/").toString());
        return mock;
    }

    public static String getResource(Class c, String path) throws IOException {
        String file = c.getResource(path).getFile();
        byte[] encoded = Files.readAllBytes(Paths.get(file));
        return new String(encoded, StandardCharsets.UTF_8);
    }


}
