package com.appstax;

import org.junit.Before;

import static org.junit.Assert.assertEquals;

public class AxConfigTest extends AxTest {

    @Before
    public void before() {
        Ax.setAppKey(APP_KEY_1);
    }

    @org.junit.Test
    public void testAppKey() {
        Ax.setAppKey(APP_KEY_2);
        assertEquals(APP_KEY_2, Ax.getAppKey());
    }

    @org.junit.Test
    public void testApiUrlSlash() {
        Ax.setApiUrl("1");
        assertEquals("1/", Ax.getApiUrl());
        Ax.setApiUrl("2/");
        assertEquals("2/", Ax.getApiUrl());
    }

}