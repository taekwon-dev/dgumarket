package com.springboot.dgumarket.dto.member;

import lombok.Getter;

@Getter
public class ChangePhoneDto {

    // 핸드폰 번호
    String phoneNumber;

    // 인증번호 (6자리 숫자 조합)
    String verificationNumber;



}
