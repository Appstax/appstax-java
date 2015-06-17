package com.appstax;

import com.squareup.okhttp.mockwebserver.MockWebServer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.assertEquals;

public class AxDateTest extends AxTest {

    @org.junit.Test
    public void shouldParseSystemDates() throws Exception {
        MockWebServer server = createMockWebServer();
        AxObject object = getObject(server);
        Date created = object.getCreated();
        Date updated = object.getUpdated();

        DateFormat target = new SimpleDateFormat("dd/MM/yyyy HH:mm (ss)");
        assertEquals("06/05/2015 10:36 (29)", target.format(created));
        assertEquals("07/05/2015 07:28 (43)", target.format(updated));

        server.shutdown();
    }

    @org.junit.Test
    public void shouldParseCustomDates() throws Exception {
        String format = "yyyy dd/MM @ HH:mm";
        String dateString1 = "1980 20/10 @ 12:13";
        String dateString2 = "2022 30/12 @ 01:02";

        AxObject object = new AxObject(COLLECTION_1);
        DateFormat source = new SimpleDateFormat(format);
        object.put("date1", source.parse(dateString1));
        object.put("date2", source.parse(dateString2));

        assertEquals(dateString1, source.format(object.getDate("date1")));
        assertEquals(dateString2, source.format(object.getDate("date2")));
    }

}
