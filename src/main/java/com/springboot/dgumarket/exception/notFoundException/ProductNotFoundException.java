package com.springboot.dgumarket.exception.NotFoundException;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(String message) {
        super(message);
    }

}

