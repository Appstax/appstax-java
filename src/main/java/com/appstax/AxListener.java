package com.appstax;

public abstract class AxListener {
    public void onOpen() {}
    public void onClose() {}
    public void onMessage(AxEvent event) {}
    public void onError(Exception e) {}
}
