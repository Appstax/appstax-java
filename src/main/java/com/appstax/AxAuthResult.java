package com.appstax;

public class AxAuthResult {

    private String authCode;
    private String redirectUri;

    public AxAuthResult(String authCode, String redirectUri) {
        this.authCode = authCode;
        this.redirectUri = redirectUri;
    }

    public String getAuthCode() {
        return authCode;
    }

    public String getRedirectUri() {
        return redirectUri;
    }
}
