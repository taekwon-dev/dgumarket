package com.springboot.dgumarket.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Created by TK YOUN (2020-11-27 오후 8:29)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */


@AllArgsConstructor
@Builder
@Getter
public class ErrorMessage {
    private int statusCode;
    private Date timestamp;
    private String message;
    private String requestPath;
    private String pathToMove;

    @Builder
    public ErrorMessage(int statusCode, Date timestamp, String message, String requestPath) {
        this.statusCode = statusCode;
        this.timestamp = timestamp;
        this.message = message;
        this.requestPath = requestPath;
    }


}

