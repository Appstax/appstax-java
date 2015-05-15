package com.appstax;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AppstaxObjectTest extends AppstaxTest {

    @Test
    public void testCollection() {
        AppstaxObject object = new AppstaxObject(COLLECTION_1);
        assertEquals(COLLECTION_1, object.getCollection());
    }

    @Test
    public void testProperties() {
        AppstaxObject object = new AppstaxObject(COLLECTION_1);
        object.put(PROPERTY_1, 1);
        object.put(PROPERTY_2, 1.1);
        object.put(PROPERTY_3, "1");
        assertEquals(1, object.get(PROPERTY_1));
        assertEquals(1.1, object.get(PROPERTY_2));
        assertEquals("1", object.get(PROPERTY_3));
    }

}