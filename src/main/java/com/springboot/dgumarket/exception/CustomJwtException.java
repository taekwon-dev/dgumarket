package com.springboot.dgumarket.exception;


/**
 * Created by TK YOUN (2020-11-28 오후 2:04)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */

public class CustomJwtException extends RuntimeException {

    public CustomJwtException(String message) {
        super(message);
    }

}
