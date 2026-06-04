package com.ch4.lumia_backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ChatCompletionException extends RuntimeException {
    private final HttpStatus status;

    public ChatCompletionException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
