package com.springboot.dgumarket.exception;

import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Date;

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
