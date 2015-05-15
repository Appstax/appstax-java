package com.appstax;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import org.junit.Test;

import static org.junit.Assert.*;
import static com.appstax.AppstaxTestHelpers.createMockWebServer;
import static com.appstax.AppstaxTestHelpers.getResource;

public class AppstaxExceptionTest {

    @Test
    public void testSaveError() throws Exception {
        MockWebServer server = createMockWebServer();
        server.enqueue(new MockResponse().setBody(getResource(getClass(), "save-object-error.json")).setResponseCode(400));

        try {
            new AppstaxObject("c").save();
        } catch(AppstaxException e) {
            assertEquals(400, e.getStatus());
            assertEquals("rzeop5miXOvMB", e.getId());
            assertEquals("ErrBadRequest", e.getCode());
            assertEquals("Hmm.", e.getMessage());
        }

        server.shutdown();
    }


}
