package com.springboot.dgumarket.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class ApiResultEntity {

    int statusCode;
    String message;
    Object responseData;


}
