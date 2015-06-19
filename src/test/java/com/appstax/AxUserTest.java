package com.appstax;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;
import org.junit.Before;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AxUserTest extends AxTest {

    @Before
    public void shouldHaveNoDefaultUser() throws Exception {
        assertEquals(null, Ax.getCurrentUser());
    }

    @org.junit.Test
    public void shouldCreateSignup() throws Exception {
        MockWebServer server = createMockWebServer();
        enqueue(1, server, 200, getResource("user-signup-success.json"));

        Ax.signup("foo", "bar");
        checkCurrentUser("foo");

        RecordedRequest req = server.takeRequest();
        assertEquals("POST", req.getMethod());
        assertEquals("/users", req.getPath());

        logout(server);
        server.shutdown();
    }

    @org.junit.Test(expected=AxException.class)
    public void ShouldThrowOnSignupError() throws Exception {
        MockWebServer server = createMockWebServer();
        enqueue(1, server, 400, getResource("user-signup-error.json"));

        Ax.signup("foo", "bar");
        assertEquals(null, Ax.getCurrentUser());
        server.shutdown();
    }

    @org.junit.Test
    public void shouldCreateLogin() throws Exception {
        MockWebServer server = createMockWebServer();
        enqueue(1, server, 200, getResource("user-login-success.json"));

        Ax.login("baz", "boo");
        checkCurrentUser("baz");

        RecordedRequest req = server.takeRequest();
        assertEquals("POST", req.getMethod());
        assertEquals("/sessions", req.getPath());

        logout(server);
        server.shutdown();
    }

    @org.junit.Test
    public void shouldThrowOnLoginError() throws Exception {
        MockWebServer server = createMockWebServer();
        enqueue(1, server, 400, getResource("user-login-error.json"));

        try {
            Ax.login("foo", "bar");
        } catch (AxException e) {
            assertEquals("Invalid username and/or password", e.getMessage());
        }

        assertEquals(null, Ax.getCurrentUser());
        server.shutdown();
    }

    @org.junit.Test
    public void shouldCreateLogout() throws Exception {
        MockWebServer server = createMockWebServer();
        enqueue(1, server, 204, getResource("user-login-success.json"));

        Ax.login("baz", "boo");
        checkCurrentUser("baz");
        RecordedRequest req = server.takeRequest();
        assertEquals("/sessions", req.getPath());

        enqueue(1, server, 204, "");
        Ax.logout();

        assertEquals(null, Ax.getCurrentUser());
        req = server.takeRequest();
        assertEquals("DELETE", req.getMethod());
        assertTrue(req.getPath().startsWith("/sessions/"));

        server.shutdown();
    }

    @org.junit.Test
    public void shouldAllowUserProperties() throws Exception {
        MockWebServer server = createMockWebServer();
        enqueue(1, server, 200, getResource("user-login-success.json"));

        Ax.login("foo", "bar");
        checkCurrentUser("foo");

        AxUser user = Ax.getCurrentUser();
        user.put("1", "2");
        assertEquals("2", user.get("1"));

        enqueue(1, server, 200, getResource("save-object-success.json"));
        Ax.save(user);

        assertEquals("2", user.get("1"));
        assertEquals("2", Ax.getCurrentUser().get("1"));

        logout(server);
        server.shutdown();
    }

    public void logout(MockWebServer server) throws Exception {
        enqueue(1, server, 200, "");
        Ax.logout();
        assertEquals(null, Ax.getCurrentUser());
    }

    public void checkCurrentUser(String name) {
        assertEquals(name, Ax.getCurrentUser().getUsername());
        assertTrue(Ax.getCurrentUser().getSessionId().length() > 0);
    }

}
