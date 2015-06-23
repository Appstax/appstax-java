package com.appstax;

public abstract class AxListener {
    void onOpen(AxEvent event) {};
    void onClose(AxEvent event) {};
    void onMessage(AxEvent event) {};
    void onFailure(AxEvent event) {};
}
