package com.springboot.dgumarket.payload.response.stomp.error;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
@Builder
public class StompErrorResponseMessage {
    private int error_code; // 에러메시지코드
    private String error_description; // 에러메시지설명
}
