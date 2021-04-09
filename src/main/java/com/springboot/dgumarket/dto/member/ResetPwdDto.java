package com.springboot.dgumarket.dto.member;

import lombok.Getter;

@Getter
public class ResetPwdDto {

    String token;              // 비밀번호 재설정 - 핸드폰 인증 시 발급 받은 토큰 값
    String newPassword;        // 새로운 비밀번호
    String checkNewPassword;   // 새로운 비밀번호 확인

}
