package com.springboot.dgumarket.exception;

public class JsonParseFailedException extends RuntimeException {

    public JsonParseFailedException(String message) {
        super(message);
    }
}
