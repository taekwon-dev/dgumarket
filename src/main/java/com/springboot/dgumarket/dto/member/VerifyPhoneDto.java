package com.springboot.dgumarket.dto.member;


import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@NoArgsConstructor
@Getter
public class VerifyPhoneDto {


    // 회원가입 절차를 진행하는 '웹메일'
    String webMail;

    // 핸드폰 번호
    String phoneNumber;

    // 인증번호 (6자리 숫자 조합)
    String verificationNumber;


}
