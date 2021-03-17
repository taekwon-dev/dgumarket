package com.springboot.dgumarket.service.sms;

import com.springboot.dgumarket.dto.member.VerifyPhoneDto;
import org.springframework.stereotype.Service;


public interface SMSService {

    void doSendSMSForPhone(VerifyPhoneDto verifyPhoneDto);



}
