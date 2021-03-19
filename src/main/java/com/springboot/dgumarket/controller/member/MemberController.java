package com.springboot.dgumarket.controller.member;

import com.springboot.dgumarket.dto.member.SignUpDto;
import com.springboot.dgumarket.payload.request.WebmailRequest;
import com.springboot.dgumarket.payload.response.ApiResponseEntity;
import com.springboot.dgumarket.service.mail.EmailService;
import com.springboot.dgumarket.service.mail.EmailServiceImpl;
import com.springboot.dgumarket.service.member.MemberProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/user/")
public class MemberController {

    @Autowired
    private MemberProfileService memberService;

    @Autowired
    private EmailService emailService;


    private static final Logger logger = LoggerFactory.getLogger(MemberController.class);

    /* 회원가입 3단계 -> 회원 가입 완료 버튼 End Point */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponseEntity> doSignUp(@RequestBody SignUpDto signUpDto) {

        // 회원가입 API 서비스 로직 실행
        memberService.doSignUp(signUpDto);


        ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                .message("회원가입이 완료되었습니다.")
                .data(null)
                .status(200)
                .build();


        return new ResponseEntity<>(apiResponseEntity, HttpStatus.CREATED);
    }

    // [회원가입 1단계 - 웹메일 인증 API]
    @PostMapping("/send-webmail")
    public ResponseEntity<ApiResponseEntity> doSendEMail(@Valid @RequestBody WebmailRequest webmailRequest) {

        // param : 받는 사람의 이메일 (동국대학교 웹메일)
        emailService.send(webmailRequest.getWebMail());


        // 클라이언트 측에서 200 Status -> Alert "입력해주신 웹메일 주소에 인증 메일을 발송했습니다"
        ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                .message("인증메일을 발송했습니다.")
                .data(null)
                .status(200)
                .build();

        return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
    }

    // [회원가입 1단계 - 웹메일 중복체크 API]
    @PostMapping("/check-webmail")
    public ResponseEntity<ApiResponseEntity> doCheckEMail(@Valid @RequestBody WebmailRequest webmailRequest) {
        // 회원가입 1단계 - 이메일 중복체크 (return ; true -> 중복된 이메일, false -> 중복되지 않은 이메일)

        boolean result = false;
        String messages = "회원가입 1단계 - 웹메일 중복체크 통과 : 회원가입 가능";

        result = memberService.doCheckWebMail(webmailRequest.getWebMail());

        // true : 중복된 웹메일 존재
        if (result) messages = "회원가입 1단계 - 웹메일 중복체크 통과 실패 : 회원가입 불가능";

        ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                .message(messages)
                .data(result)
                .status(200)
                .build();
        return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
    }

}