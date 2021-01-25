package com.springboot.dgumarket.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Created by TK YOUN (2021-01-02 오후 4:25)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */

@Builder
@AllArgsConstructor
@Getter
public class ApiResponseEntity {
    String message;
    int status;
    Object data;
}