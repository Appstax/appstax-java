package com.appstax;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;
import org.junit.Before;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AxUserTest extends AxTest {

    @Before
    public void testCurrentUser() throws Exception {
        assertEquals(null, Ax.getCurrentUser());
    }

    @org.junit.Test
    public void testSignupSuccess() throws Exception {
        MockWebServer server = createMockWebServer();
        String body = getResource("user-signup-success.json");
        server.enqueue(new MockResponse().setBody(body));

        Ax.signup("foo", "bar");
        checkCurrentUser("foo");

        RecordedRequest req = server.takeRequest();
        assertEquals("POST", req.getMethod());
        assertEquals("/users", req.getPath());

        logout(server);
        server.shutdown();
    }

    @org.junit.Test(expected=AxException.class)
    public void testSignupError() throws Exception {
        MockWebServer server = createMockWebServer();
        String body = getResource("user-signup-error.json");
        server.enqueue(new MockResponse().setBody(body).setResponseCode(400));

        Ax.signup("foo", "bar");
        assertEquals(null, Ax.getCurrentUser());
        server.shutdown();
    }

    @org.junit.Test
    public void testLoginSuccess() throws Exception {
        MockWebServer server = createMockWebServer();
        String body = getResource("user-login-success.json");
        server.enqueue(new MockResponse().setBody(body));

        Ax.login("baz", "boo");
        checkCurrentUser("baz");

        RecordedRequest req = server.takeRequest();
        assertEquals("POST", req.getMethod());
        assertEquals("/sessions", req.getPath());

        logout(server);
        server.shutdown();
    }

    @org.junit.Test
    public void testLoginError() throws Exception {
        MockWebServer server = createMockWebServer();
        String body = getResource("user-login-error.json");
        server.enqueue(new MockResponse().setBody(body).setResponseCode(400));

        try {
            Ax.login("foo", "bar");
        } catch (AxException e) {
            assertEquals("Invalid username and/or password", e.getMessage());
        }

        assertEquals(null, Ax.getCurrentUser());
        server.shutdown();
    }

    @org.junit.Test
    public void testLogoutSuccess() throws Exception {
        MockWebServer server = createMockWebServer();

        String body = getResource("user-login-success.json");
        server.enqueue(new MockResponse().setBody(body).setResponseCode(204));
        Ax.login("baz", "boo");
        checkCurrentUser("baz");
        RecordedRequest req = server.takeRequest();
        assertEquals("/sessions", req.getPath());

        server.enqueue(new MockResponse().setBody("").setResponseCode(204));
        Ax.logout();

        assertEquals(null, Ax.getCurrentUser());
        req = server.takeRequest();
        assertEquals("DELETE", req.getMethod());
        assertTrue(req.getPath().startsWith("/sessions/"));

        server.shutdown();
    }

    @org.junit.Test
    public void testPropertiesSuccess() throws Exception {
        MockWebServer server = createMockWebServer();

        String body = getResource("user-login-success.json");
        server.enqueue(new MockResponse().setBody(body));
        Ax.login("foo", "bar");
        checkCurrentUser("foo");

        AxUser user = Ax.getCurrentUser();
        user.put("1", "2");
        assertEquals("2", user.get("1"));

        body = getResource("save-object-success.json");
        server.enqueue(new MockResponse().setBody(body));
        Ax.save(user);

        assertEquals("2", user.get("1"));
        assertEquals("2", Ax.getCurrentUser().get("1"));

        logout(server);
        server.shutdown();
    }

    public void logout(MockWebServer server) throws Exception {
        server.enqueue(new MockResponse().setBody(""));
        Ax.logout();
        assertEquals(null, Ax.getCurrentUser());
    }

    public void checkCurrentUser(String name) {
        assertEquals(name, Ax.getCurrentUser().getUsername());
        assertTrue(Ax.getCurrentUser().getSessionId().length() > 0);
    }

}