package com.appstax;

import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AxUserTest extends AxTest {

    @Before
    public void blank() throws Exception {
        assertEquals(null, ax.getCurrentUser());
    }

    @Test
    public void signup() throws Exception {
        enqueue(1, 200, getResource("user-signup-success.json"));

        ax.signup("foo", "bar");
        checkCurrentUser("foo");

        RecordedRequest req = server.takeRequest();
        assertEquals("POST", req.getMethod());
        assertEquals("/users", req.getPath());

        logout(server);
    }

    @Test(expected=AxException.class)
    public void signupError() throws Exception {
        enqueue(1, 400, getResource("user-signup-error.json"));

        ax.signup("foo", "bar");
        assertEquals(null, ax.getCurrentUser());
    }

    @Test
    public void login() throws Exception {
        enqueue(1, 200, getResource("user-login-success.json"));

        ax.login("baz", "boo");
        checkCurrentUser("baz");

        RecordedRequest req = server.takeRequest();
        assertEquals("POST", req.getMethod());
        assertEquals("/sessions", req.getPath());

        logout(server);
    }

    @Test
    public void loginError() throws Exception {
        enqueue(1, 400, getResource("user-login-error.json"));

        try {
            ax.login("foo", "bar");
        } catch (AxException e) {
            assertEquals("Invalid username and/or password", e.getMessage());
        }

        assertEquals(null, ax.getCurrentUser());
    }

    @Test
    public void logout() throws Exception {
        enqueue(1, 200, getResource("user-login-success.json"));

        ax.login("baz", "boo");
        checkCurrentUser("baz");
        RecordedRequest req = server.takeRequest();
        assertEquals("/sessions", req.getPath());

        enqueue(1, 204, "");
        ax.logout();

        assertEquals(null, ax.getCurrentUser());
        req = server.takeRequest();
        assertEquals("DELETE", req.getMethod());
        assertTrue(req.getPath().startsWith("/sessions/"));

    }

    @Test
    public void properties() throws Exception {
        enqueue(1, 200, getResource("user-login-success.json"));

        ax.login("foo", "bar");
        checkCurrentUser("foo");

        AxUser user = ax.getCurrentUser();
        user.put("1", "2");
        assertEquals("2", user.get("1"));

        enqueue(1, 200, getResource("save-object-success.json"));
        ax.save(user);

        assertEquals("2", user.get("1"));
        assertEquals("2", ax.getCurrentUser().get("1"));

        logout(server);
    }

    public void logout(MockWebServer server) throws Exception {
        enqueue(1, 200, "");
        ax.logout();
        assertEquals(null, ax.getCurrentUser());
    }

    public void checkCurrentUser(String name) {
        assertEquals(name, ax.getCurrentUser().getUsername());
        assertTrue(ax.getCurrentUser().getSessionId().length() > 0);
    }

}
