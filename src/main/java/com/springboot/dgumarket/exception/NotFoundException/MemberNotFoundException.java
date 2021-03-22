package com.springboot.dgumarket.exception.NotFoundException;

public class MemberNotFoundException extends RuntimeException {

    public MemberNotFoundException(String message) {
        super(message);
    }

}
