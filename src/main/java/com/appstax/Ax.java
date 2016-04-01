package com.appstax;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * The Ax class contains all methods that create new Ax* objects,
 * as well as all methods that result in network requests.
 */
public class Ax {

    private AxClient client;
    private AxSocket socket;
    private AxSession session;
    private AxQuery query;

    /**
     * Create a new instance to communicate with the server.
     */
    public Ax(String key) {
        this(key, "https://appstax.com/api/latest/");
    }

    /**
     * Create a new instance to communicate with the server, using a custom API URL.
     */
    public Ax(String key, String url) {
        client = new AxClient(parseKey(key), parseUrl(url));
        socket = new AxSocket(client);
        session = new AxSession(client);
        query = new AxQuery(client);
    }

    /**
     * Create a new AxObject in the given collection.
     */
    public AxObject object(String collection) {
        return new AxObject(client, collection);
    }

    /**
     * Create a new AxObject in the given collection with a set of properties.
     */
    public AxObject object(String collection, JSONObject properties) {
        return new AxObject(client, collection, properties);
    }

    /**
     * Create a new AxFile object.
     */
    public AxFile file(String name, byte[] data) {
        return new AxFile(client, name, data);
    }

    /**
     * Create a new AxUser object.
     */
    public AxUser user(String username, String sessionId) {
        return new AxUser(client, username, sessionId);
    }

    /**
     * Create a new WebSocket channel.
     */
    public AxChannel channel(String name) {
        return new AxChannel(socket, name);
    }

    /**
     * Save an AxObject on the server.
     */
    public AxObject save(AxObject object) {
        return object.save();
    }

    /**
     * Save an AxObject and all its related objects on the server.
     */
    public AxObject saveAll(AxObject object) {
        for (AxObject target : object.flatten(null)) {
            target.save();
        }
        return object;
    }

    /**
     * Remove an AxObject on the server.
     */
    public AxObject remove(AxObject object) {
        return object.remove();
    }

    /**
     * Reload all the properties of an AxObject from the server.
     */
    public AxObject refresh(AxObject object) {
        return object.refresh();
    }

    /**
     * Load the actual content of an AxFile from the server.
     */
    public AxFile load(AxFile file) {
        return file.load();
    }

    /**
     * Find all the objects in a collection.
     */
    public List<AxObject> find(String collection) {
        return query.find(collection, 0);
    }

    /**
     * Find all the objects in a collection, and expand related objects to the given depth.
     */
    public List<AxObject> find(String collection, int depth) {
        return query.find(collection, depth);
    }

    /**
     * Find an object based on id in a collection.
     */
    public AxObject find(String collection, String id) {
        return query.find(collection, id, 0);
    }

    /**
     * Find an object based on id in a collection, and expand related objects to the given depth.
     */
    public AxObject find(String collection, String id, int depth) {
        return query.find(collection, id, depth);
    }

    /**
     * Find all objects in a collection that matches the given filter string.
     */
    public List<AxObject> filter(String collection, String filter) {
        return query.filter(collection, filter);
    }

    /**
     * Find all objects in a collection that matches the given properties.
     */
    public List<AxObject> filter(String collection, Map<String, String> properties) {
        return query.filter(collection, properties);
    }

    /**
     * Get the currently logged in user.
     */
    public AxUser getCurrentUser() {
        return client.getUser();
    }

    /**
     * Create and login a new user with the given username and password.
     */
    public AxUser signup(String username, String password) {
        client.setUser(session.signup(username, password));
        return client.getUser();
    }

    /**
     * Login to the account of an existing user, with the given username and password.
     */
    public AxUser login(String username, String password) {
        client.setUser(session.login(username, password));
        return client.getUser();
    }

    /**
     * Logout the current user.
     */
    public void logout() {
        if (client.getUser() != null) {
            session.logout(client.getUser());
            client.setUser(null);
        }
    }

    /**
     * Send password reset email
     */
    public void requestPasswordReset(String email) {
        session.requestPasswordReset(email);
    }

    /**
     * Change password with reset code
     */
    public AxUser changePassword(String username, String password, String code, boolean login) {
        AxUser user = session.changePassword(username, password, code, login);
        if(login && user != null) {
            client.setUser(user);
        }
        return user;
    }

    /**
     * Make sure the API key is set.
     */
    private String parseKey(String key) {
        if (key.isEmpty()) {
            throw new AxException("Empty API key");
        }
        return key;
    }

    /**
     * Normalize the given API URL.
     */
    private String parseUrl(String url) {
        if (url.isEmpty()) {
            throw new AxException("Empty API URL");
        }
        return url.replaceAll("/$", "") + "/";
    }
}
