package com.appstax;

import com.squareup.okhttp.mockwebserver.MockWebServer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import static org.junit.Assert.assertEquals;

public class AxDateTest extends AxTest {

    @org.junit.Test
    public void testFindOneSuccess() throws Exception {
        MockWebServer server = createMockWebServer();
        AxObject object = getObject(server);

        String date = object.get("sysCreated").toString();
        assertEquals("2015-05-06T10:36:29.863125116Z", date);

        DateFormat source = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        DateFormat target = new SimpleDateFormat("dd/MM/yyyy HH:mm (ss)");
        assertEquals("06/05/2015 10:36 (29)", target.format(source.parse(date)));

        server.shutdown();
    }

}
