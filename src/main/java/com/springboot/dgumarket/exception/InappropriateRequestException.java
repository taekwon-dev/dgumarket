package com.springboot.dgumarket.exception;

public class InappropriateRequestException extends RuntimeException {

    public InappropriateRequestException(String message) {
        super(message);
    }

}
