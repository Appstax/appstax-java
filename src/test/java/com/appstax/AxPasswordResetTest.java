package com.appstax;

import com.squareup.okhttp.mockwebserver.RecordedRequest;

import org.junit.Test;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class AxPasswordResetTest extends AxTest {

    @Test
    public void sendRequestWithEmail() throws Exception {
        enqueue(1, 200, "");

        ax.requestPasswordReset("my@email.com");

        RecordedRequest req = server.takeRequest();
        assertEquals("POST", req.getMethod());
        assertEquals("/users/reset/email", req.getPath());
        assertJsonEquals("{\"email\":\"my@email.com\"}", req.getBody().readUtf8());
    }

    @Test(expected=AxException.class)
    public void sendRequestWithEmailFailed() throws Exception {
        enqueue(1, 422, getResource("reset-password-error.json"));
        ax.requestPasswordReset("my@email.com");
    }

    @Test
    public void changePasswordWithoutLogin() throws Exception {
        enqueue(1, 200, "");

        ax.changePassword("foo@bar.com", "the-new-password", "the-code", false);

        RecordedRequest req = server.takeRequest();
        assertEquals("POST", req.getMethod());
        assertEquals("/users/reset/password", req.getPath());

        String expected = "{\"username\":\"foo@bar.com\""
                        + ",\"password\":\"the-new-password\""
                        + ",\"pinCode\":\"the-code\","
                        + "login:false}";
        assertJsonEquals(expected, req.getBody().readUtf8());
        assertNull(ax.getCurrentUser());
    }

    @Test(expected = AxException.class)
    public void changePasswordFails() throws Exception {
        enqueue(1, 422, getResource("reset-password-error.json"));
        ax.changePassword("foo@bar.com", "the-new-password", "the-code", false);
    }

    @Test
    public void changePasswordShouldLogInWhenRequested() throws Exception {
        enqueue(1, 200, getResource("reset-password-login-success.json"));

        assertNull(ax.getCurrentUser());

        AxUser user = ax.changePassword("foo2@bar.com", "the-new-password-2", "the-code-2", true);

        RecordedRequest req = server.takeRequest();
        assertEquals("POST", req.getMethod());
        assertEquals("/users/reset/password", req.getPath());

        String expected = "{\"username\":\"foo2@bar.com\""
                        + ",\"password\":\"the-new-password-2\""
                        + ",\"pinCode\":\"the-code-2\","
                        + "login:true}";
        assertJsonEquals(expected, req.getBody().readUtf8());

        assertNotNull(ax.getCurrentUser());
        assertSame(user, ax.getCurrentUser());
        assertEquals("iamtheuser", ax.getCurrentUser().getUsername());
        assertEquals("ieurh382934j", ax.getCurrentUser().getId());
    }

}
