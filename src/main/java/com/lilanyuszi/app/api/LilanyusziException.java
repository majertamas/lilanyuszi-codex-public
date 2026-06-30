package com.lilanyuszi.app.api;

import lombok.Getter;

@Getter
public class LilanyusziException extends Exception {

    private final Message exMessage;

    public LilanyusziException(String messageText) {
        this.exMessage = new Message(messageText, MessageSeverity.ERROR);
    }

}
