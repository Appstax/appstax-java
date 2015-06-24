package com.appstax;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AxConfigTest extends AxTest {

    @Test
    public void key() {
        Ax.setAppKey(APP_KEY_2);
        assertEquals(APP_KEY_2, Ax.getAppKey());
    }

    @Test
    public void url() {
        Ax.setApiUrl("1");
        assertEquals("1/", Ax.getApiUrl());
        Ax.setApiUrl("2/");
        assertEquals("2/", Ax.getApiUrl());
    }

    @Test
    public void socket() {
        Ax.setApiUrl("https://appstax.com/api/latest/");
        assertEquals("wss://appstax.com/api/latest/", Ax.getApiSocket());
        Ax.setApiUrl("http://appstax.com/api/latest");
        assertEquals("ws://appstax.com/api/latest/", Ax.getApiSocket());
    }

}