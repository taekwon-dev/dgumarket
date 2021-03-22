package com.springboot.dgumarket.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class ApiResultEntity {

    int resultCode;
    String message;
    Object responseData;


}
