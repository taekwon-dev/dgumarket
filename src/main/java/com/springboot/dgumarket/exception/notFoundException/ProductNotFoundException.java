package com.springboot.dgumarket.exception.notFoundException;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(String message) {
        super(message);
    }

}

