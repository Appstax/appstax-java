package com.appstax;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;

import static org.junit.Assert.assertEquals;

public class AxExceptionTest extends AxTest {

    @org.junit.Test
    public void shouldThrowOnServerError() throws Exception {
        MockWebServer server = createMockWebServer();
        enqueue(1, server, 400, getResource("save-object-error.json"));

        try {
            Ax.save(new AxObject("c"));
        } catch(AxException e) {
            assertEquals(400, e.getStatus());
            assertEquals("rzeop5miXOvMB", e.getId());
            assertEquals("ErrBadRequest", e.getCode());
            assertEquals("Hmm.", e.getMessage());
        }

        server.shutdown();
    }


}
