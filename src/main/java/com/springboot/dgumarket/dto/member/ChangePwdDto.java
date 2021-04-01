package com.springboot.dgumarket.dto.member;

import lombok.Getter;

@Getter
public class ChangePwdDto {
    String prevPassword;       // 회원의 이전 비밀번호
    String newPassword;        // 새로운 비밀번호
    String checkNewPassword;   // 새로운 비밀번호 확인
}
