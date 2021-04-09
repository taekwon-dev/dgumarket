package com.springboot.dgumarket.service.sms;

import com.springboot.dgumarket.dto.member.ChangePhoneDto;
import com.springboot.dgumarket.dto.member.FindPwdDto;
import com.springboot.dgumarket.dto.member.VerifyPhoneDto;
import com.springboot.dgumarket.payload.response.ApiResultEntity;
import org.springframework.stereotype.Service;


public interface SMSService {

    // 회원가입 2단계 - 핸드폰 인증문자 발송 API
    ApiResultEntity doSendSMSForPhone(VerifyPhoneDto verifyPhoneDto);

    // 핸드폰 변경 - 새로운 핸드폰 대상 인증문자 발송 API
    ApiResultEntity doSendSMSForChangePhone(int userId, ChangePhoneDto changePhoneDto);

    // 비밀번호 찾기 - 핸드폰 인증문자 발송 API
    ApiResultEntity doSendSMSForFindPwd(FindPwdDto findPwdDto);



}
