package com.springboot.dgumarket.controller.sms;

import com.springboot.dgumarket.dto.member.ChangePhoneDto;
import com.springboot.dgumarket.dto.member.FindPwdDto;
import com.springboot.dgumarket.dto.member.VerifyPhoneDto;
import com.springboot.dgumarket.payload.response.ApiResponseEntity;
import com.springboot.dgumarket.payload.response.ApiResultEntity;
import com.springboot.dgumarket.service.UserDetailsImpl;
import com.springboot.dgumarket.service.sms.SMSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/send-sms")
public class SendSMSController {

    @Autowired
    private SMSService smsService;

    // [회원가입 2단계 - 핸드폰 인증문자 발송 API]
    // Attribute - produces : 응답 데이터 타입 제한 / consumes : 요청 데이터 타입 제한
    @PostMapping(value = "/verify-phone", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResultEntity> doSendSMSForPhone(@RequestBody VerifyPhoneDto verifyPhoneDto) {

        ApiResultEntity apiResultEntity = smsService.doSendSMSForPhone(verifyPhoneDto);
        return new ResponseEntity<>(apiResultEntity, HttpStatus.OK);
    }

    // 핸드폰 번호 변경 API
    @PostMapping(value = "/change-phone", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResultEntity> doSendSMSForChangePhone(Authentication authentication, @RequestBody ChangePhoneDto changePhoneDto) {

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        ApiResultEntity apiResultEntity = smsService.doSendSMSForChangePhone(userDetails.getId(), changePhoneDto);
        return new ResponseEntity<>(apiResultEntity, HttpStatus.OK);

    }

    // 비밀번호 찾기 - 핸드폰 인증문자 발송 API
    @PostMapping(value = "/find-pwd", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResultEntity> doSendSMSForFindPwd(@RequestBody FindPwdDto findPwdDto) {

        ApiResultEntity apiResultEntity = smsService.doSendSMSForFindPwd(findPwdDto);
        return new ResponseEntity<>(apiResultEntity, HttpStatus.OK);

    }



}
