package com.appstax;

import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;
import org.junit.Before;
import org.junit.Test;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
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

    @Test
    public void shouldGetFacebookProviderConfig() throws Exception {
        enqueue(1, 200, "{\"clientId\":\"123456789\"}");

        AxAuthConfig config = ax.getAuthConfig("facebook");

        RecordedRequest req = server.takeRequest();
        assertEquals("GET", req.getMethod());
        assertEquals("/sessions/providers/facebook", req.getPath());

        assertEquals("oauth", config.getType());
        assertEquals("123456789", config.getClientId());
        assertEquals("https://www.facebook.com/dialog/oauth?client_id={clientId}&redirect_uri={redirectUri}&scope=public_profile,email", config.getUri());
        assertEquals("https://appstax.com/api/latest/sessions/auth", config.getRedirectUri());
    }

    @Test
    public void shouldGetGoogleProviderConfig() throws Exception {
        enqueue(1, 200, "{\"clientId\":\"987654321\"}");

        AxAuthConfig config = ax.getAuthConfig("google");

        RecordedRequest req = server.takeRequest();
        assertEquals("GET", req.getMethod());
        assertEquals("/sessions/providers/google", req.getPath());

        assertEquals("oauth", config.getType());
        assertEquals("987654321", config.getClientId());
        assertEquals("https://accounts.google.com/o/oauth2/v2/auth?client_id={clientId}&redirect_uri={redirectUri}&nonce={nonce}&response_type=code&scope=profile+email", config.getUri());
        assertEquals("https://appstax.com/api/latest/sessions/auth", config.getRedirectUri());
    }

    @Test
    public void loginWithFacebook() throws Exception {
        enqueue(1, 200, getResource("login-provider-success.json"));

        ax.loginWithProvider("facebook", new AxAuthResult("the-auth-code", "redirect/uri"));

        RecordedRequest req = server.takeRequest();
        assertEquals("POST", req.getMethod());
        assertEquals("/sessions", req.getPath());

        String expected = "{\"sysProvider\":{"
                        + "\"type\":\"facebook\","
                        + "\"data\":{"
                        + "\"code\":\"the-auth-code\","
                        + "\"redirectUri\":\"redirect/uri\""
                        + "}}}";
        assertJsonEquals(expected, req.getBody().readUtf8());

        assertCurrentUser("iamtheuser", "MDNkNDA5NGEtZjJmZi00Y2NmLTdkMTktMTMwZWY1NDFiYTA4");
    }

    @Test
    public void loginWithGoogle() throws Exception {
        enqueue(1, 200, getResource("login-provider-success.json"));

        ax.loginWithProvider("google", new AxAuthResult("the-auth-code-2", "redirect/uri/2"));

        RecordedRequest req = server.takeRequest();
        assertEquals("POST", req.getMethod());
        assertEquals("/sessions", req.getPath());

        String expected = "{\"sysProvider\":{"
                + "\"type\":\"google\","
                + "\"data\":{"
                + "\"code\":\"the-auth-code-2\","
                + "\"redirectUri\":\"redirect/uri/2\""
                + "}}}";
        assertJsonEquals(expected, req.getBody().readUtf8());

        assertCurrentUser("iamtheuser", "MDNkNDA5NGEtZjJmZi00Y2NmLTdkMTktMTMwZWY1NDFiYTA4");
    }

    @Test(expected = AxException.class)
    public void loginWithProviderFails() throws Exception {
        enqueue(1, 422, getResource("login-provider-error.json"));
        ax.loginWithProvider("facebook", new AxAuthResult("the-auth-code", "redirect/uri"));
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

    public void assertCurrentUser(String username, String sessionId) {
        assertEquals(username, ax.getCurrentUser().getUsername());
        assertEquals(sessionId, ax.getCurrentUser().getSessionId());
    }

}
