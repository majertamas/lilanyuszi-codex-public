package com.lilanyuszi.app.config;

import com.lilanyuszi.app.api.LilanyusziException;
import com.lilanyuszi.app.api.Message;
import com.lilanyuszi.app.api.MessageSeverity;
import com.lilanyuszi.app.api.RestResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Collections;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RestResponse<Void>> handle(Exception ex) {

        log.info("EXCEPTION OCCURRED, MESSAGE: {}", ex.getMessage());
        RestResponse<Void> response = new RestResponse<>();
        response.setMessages(addMessages(ex));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    private List<Message> addMessages(Exception ex) {
        if (ex instanceof LilanyusziException lilanyusziException) {
            return Collections.singletonList(lilanyusziException.getExMessage());
        } else if (ex instanceof MethodArgumentNotValidException methodArgumentNotValidException) {
            return methodArgumentNotValidException.getBindingResult().getFieldErrors().stream()
                    .map(error -> new Message(error.getDefaultMessage(), MessageSeverity.ERROR))
                    .toList();
        } else {
            return Collections.singletonList(new Message(ex.getMessage(), MessageSeverity.ERROR));
        }
    }
}
