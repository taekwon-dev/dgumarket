package com.springboot.dgumarket.payload.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

/**
 * Created by TK YOUN (2020-11-09 오후 9:53)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 *
 * 회원가입 1단계 - 웹메일 체크
 */

@Getter
@Setter
public class WebmailRequest {

    @NotBlank
    String webMail;

}
