package com.appstax;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AppstaxTest {

    private static final String APP_KEY_1 = "YourAppKey";
    private static final String APP_KEY_2 = "SomeAppKey";

    @Before
    public void before() {
        Appstax.setAppKey(APP_KEY_1);
    }

    @Test
    public void testAppKey() {
        Appstax.setAppKey(APP_KEY_2);
        assertEquals(APP_KEY_2, Appstax.getAppKey());
    }

    @Test
    public void testApiUrlSlash() {
        Appstax.setApiUrl("1");
        assertEquals("1/", Appstax.getApiUrl());
        Appstax.setApiUrl("2/");
        assertEquals("2/", Appstax.getApiUrl());
    }

}