package com.springboot.dgumarket.exception.stomp;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class StompErrorException extends Exception{

    private int ERR_CODE;
    private String ERR_MESSAGE;

}
