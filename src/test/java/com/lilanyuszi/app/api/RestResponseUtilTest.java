package com.lilanyuszi.app.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RestResponseUtilTest {

    public static final String LOADED = "Loaded";
    public static final String SAVED = "Saved";
    public static final String PAYLOAD = "payload";

    @Test
    void createRestResponseWithMessageCreatesMessageOnlyResponse() {
        RestResponse<Void> response = RestResponseUtil.createRestResponse(SAVED, MessageSeverity.SUCCESS);

        assertNull(response.getData());
        assertEquals(1, response.getMessages().size());
        assertEquals(SAVED, response.getMessages().get(0).getText());
        assertEquals(MessageSeverity.SUCCESS, response.getMessages().get(0).getSeverity());
    }

    @Test
    void createRestResponseWithMessageAndDataCreatesResponseWithBoth() {
        String data = PAYLOAD;

        RestResponse<String> response = RestResponseUtil.createRestResponse(LOADED, MessageSeverity.INFO, data);

        assertSame(data, response.getData());
        assertEquals(1, response.getMessages().size());
        assertEquals(LOADED, response.getMessages().get(0).getText());
        assertEquals(MessageSeverity.INFO, response.getMessages().get(0).getSeverity());
    }

    @Test
    void createRestResponseWithDataCreatesDataOnlyResponse() {
        String data = PAYLOAD;

        RestResponse<String> response = RestResponseUtil.createRestResponse(data);

        assertSame(data, response.getData());
        assertTrue(response.getMessages().isEmpty());
    }
}
