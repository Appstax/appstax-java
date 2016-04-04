package com.appstax;

public final class AxAuthConfig {

    private String type;
    private String uri;
    private String redirectUri;
    private String clientId;

    private AxAuthConfig() {

    }

    public AxAuthConfig(String type, String uri, String redirectUri, String clientId) {
        this.type = type;
        this.uri = uri;
        this.redirectUri = redirectUri;
        this.clientId = clientId;
    }

    public static AxAuthConfig config(String provider, String clientId) {
        AxAuthConfig config = new AxAuthConfig();
        config.type = "oauth";
        config.clientId = clientId;
        config.redirectUri = "https://appstax.com/api/latest/sessions/auth";
        switch (provider) {
            case "facebook":
                config.uri = "https://www.facebook.com/dialog/oauth?client_id={clientId}&redirect_uri={redirectUri}&scope=public_profile,email";
                break;
            case "google":
                config.uri = "https://accounts.google.com/o/oauth2/v2/auth?client_id={clientId}&redirect_uri={redirectUri}&nonce={nonce}&response_type=code&scope=profile+email";
                break;
        }
        return config;
    }

    public String getType() {
        return type;
    }

    public String getUri() {
        return uri;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public String getClientId() {
        return clientId;
    }
}
