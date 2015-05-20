package com.appstax;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AppstaxUserTest extends AppstaxTest {

    @Before
    public void testCurrentUser() throws Exception {
        assertEquals(null, Appstax.getCurrentUser());
    }

    @Test
    public void testSignupSuccess() throws Exception {
        MockWebServer server = createMockWebServer();
        String body = getResource("user-signup-success.json");
        server.enqueue(new MockResponse().setBody(body));

        Appstax.signup("foo", "bar");
        checkCurrentUser("foo");

        RecordedRequest req = server.takeRequest();
        assertEquals("POST", req.getMethod());
        assertEquals("/users", req.getPath());

        logout(server);
        server.shutdown();
    }

    @Test(expected=AppstaxException.class)
    public void testSignupError() throws Exception {
        MockWebServer server = createMockWebServer();
        String body = getResource("user-signup-error.json");
        server.enqueue(new MockResponse().setBody(body).setResponseCode(400));

        Appstax.signup("foo", "bar");
        assertEquals(null, Appstax.getCurrentUser());
        server.shutdown();
    }

    @Test
    public void testLoginSuccess() throws Exception {
        MockWebServer server = createMockWebServer();
        String body = getResource("user-login-success.json");
        server.enqueue(new MockResponse().setBody(body));

        Appstax.login("baz", "boo");
        checkCurrentUser("baz");

        RecordedRequest req = server.takeRequest();
        assertEquals("POST", req.getMethod());
        assertEquals("/sessions", req.getPath());

        logout(server);
        server.shutdown();
    }

    @Test
    public void testLoginError() throws Exception {
        MockWebServer server = createMockWebServer();
        String body = getResource("user-login-error.json");
        server.enqueue(new MockResponse().setBody(body).setResponseCode(400));

        try {
            Appstax.login("foo", "bar");
        } catch (AppstaxException e) {
            assertEquals("Invalid username and/or password", e.getMessage());
        }

        assertEquals(null, Appstax.getCurrentUser());
        server.shutdown();
    }

    @Test
    public void testLogoutSuccess() throws Exception {
        MockWebServer server = createMockWebServer();

        String body = getResource("user-login-success.json");
        server.enqueue(new MockResponse().setBody(body).setResponseCode(204));
        Appstax.login("baz", "boo");
        checkCurrentUser("baz");
        RecordedRequest req = server.takeRequest();
        assertEquals("/sessions", req.getPath());

        server.enqueue(new MockResponse().setBody("").setResponseCode(204));
        Appstax.logout();

        assertEquals(null, Appstax.getCurrentUser());
        req = server.takeRequest();
        assertEquals("DELETE", req.getMethod());
        assertTrue(req.getPath().startsWith("/sessions/"));

        server.shutdown();
    }

    @Test
    public void testPropertiesSuccess() throws Exception {
        MockWebServer server = createMockWebServer();

        String body = getResource("user-login-success.json");
        server.enqueue(new MockResponse().setBody(body));
        Appstax.login("foo", "bar");
        checkCurrentUser("foo");

        AppstaxUser user = Appstax.getCurrentUser();
        user.put("1", "2");
        assertEquals("2", user.get("1"));

        body = getResource("save-object-success.json");
        server.enqueue(new MockResponse().setBody(body));
        Appstax.save(user);

        assertEquals("2", user.get("1"));
        assertEquals("2", Appstax.getCurrentUser().get("1"));

        logout(server);
        server.shutdown();
    }

    public void logout(MockWebServer server) throws Exception {
        server.enqueue(new MockResponse().setBody(""));
        Appstax.logout();
        assertEquals(null, Appstax.getCurrentUser());
    }

    public void checkCurrentUser(String name) {
        assertEquals(name, Appstax.getCurrentUser().getUsername());
        assertTrue(Appstax.getCurrentUser().getSessionId().length() > 0);
    }

}
