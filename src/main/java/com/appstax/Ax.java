package com.appstax;

import org.json.JSONObject;

import java.util.*;

public class Ax {

    private AxClient client;
    private AxSocket socket;
    private AxSession session;
    private AxQuery query;

    public Ax(String key) {
        this(key, "https://appstax.com/api/latest/");
    }

    public Ax(String key, String url) {
        client = new AxClient(parseKey(key), parseUrl(url));
        socket = new AxSocket(client);
        session = new AxSession(client);
        query = new AxQuery(client);
    }

    public AxObject object(String collection) {
        return new AxObject(client, collection);
    }

    public AxObject object(String collection, JSONObject properties) {
        return new AxObject(client, collection, properties);
    }

    public AxFile file(String name, byte[] data) {
        return new AxFile(client, name, data);
    }

    public AxUser user(String username, String sessionId) {
        return new AxUser(client, username, sessionId);
    }

    public AxChannel channel(String name, AxListener listener) {
        return new AxChannel(socket, name, listener);
    }

    public AxObject save(AxObject object) {
        return object.save();
    }

    public AxObject saveAll(AxObject object) {
        for (AxObject target : object.flatten(null)) {
            target.save();
        }
        return object;
    }

    public AxObject remove(AxObject object) {
        return object.remove();
    }

    public AxObject refresh(AxObject object) {
        return object.refresh();
    }

    public AxFile load(AxFile file) {
        return file.load();
    }

    public List<AxObject> find(String collection) {
        return query.find(collection, 0);
    }

    public List<AxObject> find(String collection, int depth) {
        return query.find(collection, depth);
    }

    public AxObject find(String collection, String id) {
        return query.find(collection, id, 0);
    }

    public AxObject find(String collection, String id, int depth) {
        return query.find(collection, id, depth);
    }

    public List<AxObject> filter(String collection, String filter) {
        return query.filter(collection, filter);
    }

    public List<AxObject> filter(String collection, Map<String, String> properties) {
        return query.filter(collection, properties);
    }

    public AxUser getCurrentUser() {
        return client.getUser();
    }

    public AxUser signup(String username, String password) {
        client.setUser(session.signup(username, password));
        return client.getUser();
    }

    public AxUser login(String username, String password) {
        client.setUser(session.login(username, password));
        return client.getUser();
    }

    public void logout() {
        if (client.getUser() != null) {
            session.logout(client.getUser());
            client.setUser(null);
        }
    }

    private String parseKey(String key) {
        if (key.isEmpty()) {
            throw new AxException("Empty API key");
        }
        return key;
    }

    private String parseUrl(String url) {
        if (url.isEmpty()) {
            throw new AxException("Empty API URL");
        }
        return url.replaceAll("/$", "") + "/";
    }

}
