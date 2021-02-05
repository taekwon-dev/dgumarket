package com.springboot.dgumarket.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
public class CustomControllerExecption extends Exception{

    private String message;
    private HttpStatus httpStatus;

    public CustomControllerExecption(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
