package com.springboot.dgumarket.service.sms;

import com.springboot.dgumarket.dto.member.ChangePhoneDto;
import com.springboot.dgumarket.dto.member.VerifyPhoneDto;
import com.springboot.dgumarket.payload.response.ApiResultEntity;
import org.springframework.stereotype.Service;


public interface SMSService {

    void doSendSMSForPhone(VerifyPhoneDto verifyPhoneDto);

    ApiResultEntity doSendSMSForChangePhone(int userId, ChangePhoneDto changePhoneDto);



}
