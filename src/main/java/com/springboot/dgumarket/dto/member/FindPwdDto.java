package com.springboot.dgumarket.dto.member;


import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;


@Getter
public class FindPwdDto {

    // 비밀번호 찾기 요청 '웹메일'
    String webMail;

    // 핸드폰 번호
    String phoneNumber;

    // 인증번호 (6자리 숫자 조합)
    String verificationNumber;

    @QueryProjection
    public FindPwdDto(String webMail, String phoneNumber) {
        this.webMail = webMail;
        this.phoneNumber = phoneNumber;
    }

}
