package com.appstax;

import org.json.JSONException;
import org.junit.Test;

import static org.junit.Assert.*;

public class AxObjectTest extends AxTest {

    @Test
    public void collection() {
        AxObject object = ax.object(COLLECTION_1);
        assertEquals(COLLECTION_1, object.getCollection());
    }

    @Test
    public void properties() {
        AxObject object = ax.object(COLLECTION_1);
        object.put(PROPERTY_1, 1);
        object.put(PROPERTY_2, 1.1);
        object.put(PROPERTY_3, "1");

        assertTrue(object.has(PROPERTY_1));
        assertFalse(object.has(PROPERTY_1 + "1"));

        assertEquals(1, object.get(PROPERTY_1));
        assertEquals(1.1, object.get(PROPERTY_2));
        assertEquals("1", object.get(PROPERTY_3));
        assertEquals("1", object.getString(PROPERTY_3));

        try {
            object.getString(PROPERTY_1);
        } catch (JSONException e) {
            assertNotNull(e);
        }
    }

}