package com.springboot.dgumarket.controller.member;

import com.springboot.dgumarket.dto.member.SignUpDto;
import com.springboot.dgumarket.payload.request.LoginRequest;
import com.springboot.dgumarket.payload.request.WebmailRequest;
import com.springboot.dgumarket.service.LoginLoggedService;
import com.springboot.dgumarket.service.mail.EmailServiceImpl;
import com.springboot.dgumarket.service.member.MemberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * Created by TK YOUN (2020-10-20 오전 8:44)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 *
 * API
 * 회원가입 -> /api/auth/signup
 * 로그인 -> /api/auth/login
 * 로그아웃 -> WebSecurityConfig.class -> /api/auth/logout 으로 설정
 *            로그아웃 요청 시 -> security/CustomLogoutSuccessHandler.class에서 처리
 *            (로그아웃 시 요청한 브라우저 토큰 삭제 & 로그아웃 처리 후 Redirect)
 *
 */

@RestController
@RequestMapping("/user/")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private LoginLoggedService loginLoggedService;

    @Autowired
    private EmailServiceImpl emailService;


    private static final Logger logger = LoggerFactory.getLogger(MemberController.class);

    /* 회원가입 3단계 -> 회원 가입 완료 버튼 End Point */
    @PostMapping("/signup")
    public ResponseEntity<String> doSignUp(@RequestBody SignUpDto signUpDto) {

        SignUpDto udt = memberService.doSignUp(signUpDto);
        return new ResponseEntity<>("Successful", HttpStatus.CREATED);
    }

    /* 로그인 End point */
    @PostMapping("/login")
    public ResponseEntity<?> doLogin(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {

        logger.debug("doLogin() is called");
        logger.debug("LoginRequest[webMail] : " + loginRequest.getWebMail());
        logger.debug("LoginRequest[Password] : " + loginRequest.getPassword());

        /* LoginLoggedService -> doLogLogin() -> DB에 로그 정보 저장, 로그인 성공 시 토큰 발행, 쿠키 생성 처리 */
        loginLoggedService.doLogLogin(loginRequest, response);

        return new ResponseEntity<>("Successful", HttpStatus.OK);
    }

    @PostMapping("/send-webmail")
    public void doSendEMail(@Valid @RequestBody WebmailRequest webmailRequest) {
        emailService.send(webmailRequest.getWebMail());
    }

    @PostMapping("/check-webmail")
    public boolean doCheckEMail(@Valid @RequestBody WebmailRequest webmailRequest) {
        // 회원가입 1단계 - 이메일 중복체크 (return ; true -> 중복된 이메일, false -> 중복되지 않은 이메일)
        return memberService.doCheckWebMail(webmailRequest.getWebMail());
    }
    
}
