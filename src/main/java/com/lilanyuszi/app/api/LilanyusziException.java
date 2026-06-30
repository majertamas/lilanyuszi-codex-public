package com.lilanyuszi.app.api;

import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
public class LilanyusziException extends Exception {

    private final List<Message> messages;

    public LilanyusziException(String messageText) {
        this.messages = Collections.singletonList(new Message(messageText, MessageSeverity.ERROR));
    }

}
