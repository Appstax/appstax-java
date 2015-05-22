package com.appstax;

import static org.junit.Assert.assertEquals;

public class AxObjectTest extends AxTest {

    @org.junit.Test
    public void testCollection() {
        AxObject object = new AxObject(COLLECTION_1);
        assertEquals(COLLECTION_1, object.getCollection());
    }

    @org.junit.Test
    public void testProperties() {
        AxObject object = new AxObject(COLLECTION_1);
        object.put(PROPERTY_1, 1);
        object.put(PROPERTY_2, 1.1);
        object.put(PROPERTY_3, "1");
        assertEquals(1, object.get(PROPERTY_1));
        assertEquals(1.1, object.get(PROPERTY_2));
        assertEquals("1", object.get(PROPERTY_3));
    }

}