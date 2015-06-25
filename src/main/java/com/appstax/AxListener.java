package com.appstax;

public abstract class AxListener {
    public void onOpen(AxEvent event) {};
    public void onClose(AxEvent event) {};
    public void onMessage(AxEvent event) {};
    public void onFailure(AxEvent event) {};
}
