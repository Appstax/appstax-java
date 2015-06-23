package com.appstax;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AxExceptionTest extends AxTest {

    @Test
    public void badReq() throws Exception {
        enqueue(1, 400, getResource("save-object-error.json"));

        try {
            Ax.save(new AxObject("c"));
        } catch(AxException e) {
            assertEquals(400, e.getStatus());
            assertEquals("rzeop5miXOvMB", e.getId());
            assertEquals("ErrBadRequest", e.getCode());
            assertEquals("Hmm.", e.getMessage());
        }
    }


}
