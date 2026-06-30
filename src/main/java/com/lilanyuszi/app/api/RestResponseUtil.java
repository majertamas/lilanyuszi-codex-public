package com.lilanyuszi.app.api;

import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class RestResponseUtil {

    public static RestResponse<Void> createRestResponse(String text, MessageSeverity severity) {
        Message message = new Message(text, severity);
        return new RestResponse<>(message);
    }

    public static <T> RestResponse<T> createRestResponse(String text, MessageSeverity severity, T data) {
        Message message = new Message(text, severity);
        RestResponse<T> response = new RestResponse<>();
        response.setMessages(List.of(message));
        response.setData(data);
        return response;
    }

    public static <T> RestResponse<T> createRestResponse(T data) {
        return new RestResponse<>(data);
    }

    public static RestResponse<Void> createVoidRestResponse() {
        return new RestResponse<>();
    }

}
