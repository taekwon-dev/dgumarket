package com.springboot.dgumarket.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;


@Getter
public class CustomControllerExecption extends RuntimeException {

    private String message;
    private HttpStatus httpStatus;
    private String moveToPath;
    private int customStatusCode; 

    public CustomControllerExecption(String message, HttpStatus httpStatus, @Nullable String moveToPath, int customStatusCode) {
        this.message = message;
        this.httpStatus = httpStatus;
        this.moveToPath = moveToPath;
        this.customStatusCode = customStatusCode;
    }
}
