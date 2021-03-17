package com.springboot.dgumarket.service.premember;

import com.springboot.dgumarket.dto.member.SignUpDto;
import com.springboot.dgumarket.dto.member.VerifyPhoneDto;

public interface PreMemberService {

    // 핸드폰 중복체크
    boolean doCheckDuplicatePhone(VerifyPhoneDto verifyPhoneDto);

    // 핸드폰 번호 인증
    String doVerifyNumberForPhone(VerifyPhoneDto verifyPhoneDto);

    // 닉네임 중복체크
    boolean doCheckDupilicateNickname(SignUpDto signUpDto);




}
