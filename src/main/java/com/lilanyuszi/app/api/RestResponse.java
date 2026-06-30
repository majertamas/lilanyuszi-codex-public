package com.lilanyuszi.app.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestResponse<T> {

    private List<Message> messages = new LinkedList<>();
    private T data;

    public RestResponse(Message message) {
        this.messages = List.of(message);
    }

    public RestResponse(T data) {
        this.data = data;
    }
}
