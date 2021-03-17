package com.springboot.dgumarket.controller.sms;

import com.springboot.dgumarket.dto.member.VerifyPhoneDto;
import com.springboot.dgumarket.payload.response.ApiResponseEntity;
import com.springboot.dgumarket.service.sms.SMSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ApiResponseEntity> doSendSMSForPhone(@RequestBody VerifyPhoneDto verifyPhoneDto) {

        smsService.doSendSMSForPhone(verifyPhoneDto);

        ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                .message("인증문자가 발송되었습니다.")
                .data(null)
                .status(200)
                .build();


        return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
    }
}
