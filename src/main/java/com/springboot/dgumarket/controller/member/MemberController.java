package com.springboot.dgumarket.controller.member;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.springboot.dgumarket.dto.member.FindPwdDto;
import com.springboot.dgumarket.dto.member.ResetPwdDto;
import com.springboot.dgumarket.dto.member.SignUpDto;

import com.springboot.dgumarket.exception.ErrorMessage;
import com.springboot.dgumarket.exception.notFoundException.PreMemberNotFoundException;
import com.springboot.dgumarket.model.chat.ChatMessage;
import com.springboot.dgumarket.model.chat.ChatRoom;
import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.payload.request.WebmailRequest;

import com.springboot.dgumarket.payload.response.ApiResultEntity;
import com.springboot.dgumarket.repository.member.BlockUserRepository;
import com.springboot.dgumarket.repository.member.MemberRepository;
import com.springboot.dgumarket.service.mail.EmailService;

import com.springboot.dgumarket.service.member.MemberProfileService;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.mail.MailException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;

@RestController
@RequestMapping("/api/user/")
public class MemberController {

    @Autowired
    private MemberProfileService memberService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BlockUserRepository blockUserRepository;


    private static final Logger logger = LoggerFactory.getLogger(MemberController.class);

    /* 회원가입 3단계 -> 회원 가입 완료 버튼 End Point */
    @PostMapping("/signup")
    public ResponseEntity<ApiResultEntity> doSignUp(@RequestBody SignUpDto signUpDto) {


        // 3단계 페이지 접근 시, 이메일 인증 링크를 누른 브라우저 외 다른 브라우저에서 접근 시 핸드폰 번호 값을 활용할 수 없으므로, 잘못된 접근으로 처리
        if (signUpDto.getPhoneNumber() == null) throw new PreMemberNotFoundException(errorResponse("회원 절차에 있는 예비 회원정보를 찾을 수 없는 경우", 301, "/api/user/signup"));


        // 회원가입 API 서비스 로직 실행
        memberService.doSignUp(signUpDto);


        ApiResultEntity apiResponseEntity = ApiResultEntity.builder()
                .statusCode(200)
                .message("회원가입이 완료되었습니다.")
                .responseData(null)
                .build();


        return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
    }

    // [회원가입 1단계 - 웹메일 인증 API]
    @PostMapping("/send-webmail")
    public ResponseEntity<ApiResultEntity> doSendEMail(@Valid @RequestBody WebmailRequest webmailRequest) {

        ApiResultEntity apiResponseEntity = null;
        try {
            emailService.send(webmailRequest.getWebMail());

            apiResponseEntity = ApiResultEntity.builder()
                    .statusCode(1)
                    .message("인증메일을 발송했습니다.")
                    .responseData(null)
                    .build();

        } catch (MailException | IOException | MessagingException | TemplateException e)  {
            apiResponseEntity = ApiResultEntity.builder()
                    .statusCode(2)
                    .message("인증메일 발송 실패했습니다. 잠시 후 다시 시도해주세요.")
                    .responseData(null)
                    .build();
        }

        return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
    }

    // [회원가입 1단계 - 웹메일 중복체크 API]
   @PostMapping("/check-webmail")
    public ResponseEntity<ApiResultEntity> doCheckEMail(@Valid @RequestBody WebmailRequest webmailRequest) {
        // 회원가입 1단계 - 이메일 중복체크 (return ; true -> 중복된 이메일, false -> 중복되지 않은 이메일)

       boolean result = false;

       result = memberService.doCheckWebMail(webmailRequest.getWebMail());


       if (result) {

           ApiResultEntity apiResponseEntity = ApiResultEntity.builder()
                   .statusCode(1)
                   .message("회원가입 1단계 - 웹메일 중복체크 통과 실패 : 회원가입 불가능")
                   .responseData(null)
                   .build();

           return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
       } else {
           ApiResultEntity apiResponseEntity = ApiResultEntity.builder()
                   .statusCode(2)
                   .message("회원가입 1단계 - 웹메일 중복체크 통과 : 회원가입 가능")
                   .responseData(null)
                   .build();

           return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
       }
    }

    // 비밀번호 재설정 API 중 핸드폰 인증
    @PostMapping("/find-pwd/verify-phone")
    public ResponseEntity<ApiResultEntity> doVerifyPhoneForFindPwd(@RequestBody FindPwdDto findPwdDto) {
        ApiResultEntity apiResultEntity = memberService.checkVerificationPhoneForFindPwd(findPwdDto);
        return new ResponseEntity<>(apiResultEntity, HttpStatus.OK);
    }

    // 비밀번호 재설정 API
    @PostMapping("/find-pwd")
    public ResponseEntity<ApiResultEntity> doResetPasswordForFindPwd(@RequestBody ResetPwdDto resetPwdDto) {
        ApiResultEntity apiResultEntity = memberService.resetPasswordForFindPwd(resetPwdDto);
        return new ResponseEntity<>(apiResultEntity, HttpStatus.OK);
    }


    // 회원 탈퇴 이후, 30일 보호 기간이 끝난 계정 삭제 처리 API (임시)
    // 인터셉터 통과 경로 값에서 제외 처리 해놓은 상태
    @PostMapping("/delete-member")
    public ResponseEntity<?> deleteMember() {

        // 멤버 - 채팅&채팅메시지

        // 멤버 삭제 -> 상품 삭제 -> 해당 채팅방에서 문제
        Member member = memberRepository.findByWebMail("taekwon@dongguk.edu");

         // Caution : CuncurrentModificationException
        for (ChatRoom consumerChatRoom : member.getConsumerChatRooms()) {
            member.disconnConsumerToChatRoom(consumerChatRoom);
        }
        member.getConsumerChatRooms().removeAll(member.getConsumerChatRooms());

        // Caution : CuncurrentModificationException
        for (ChatRoom sellerChatRoom : member.getSellerChatRooms()) {
            member.disconnSellerToChatRoom(sellerChatRoom);
        }
        member.getSellerChatRooms().removeAll(member.getSellerChatRooms());

        // Caution : CuncurrentModificationException
        for (ChatMessage senderMessage : member.getSenderMessages()) {
            member.disconnSenderToChatMsg(senderMessage);
        }
        member.getSenderMessages().removeAll(member.getSenderMessages());


        // Caution : CuncurrentModificationException
        for (ChatMessage receiverMessage : member.getReceiverMessages()) {
            member.disconnReceiverToChatMsg(receiverMessage);
        }
        member.getReceiverMessages().removeAll(member.getReceiverMessages());


        memberRepository.delete(member);
        return new ResponseEntity<>("회원 삭제", HttpStatus.OK);
    }

    public String errorResponse(String errMsg, int resultCode, String requestPath) {

        // [ErrorMessage]
        // {
        //     int statusCode;
        //     Date timestamp;
        //     String message;
        //     String requestPath;
        //     String pathToMove;
        // }

        // errorCode에 따라서 예외 결과 클라이언트가 특정 페이지로 요청해야 하는 경우가 있다.
        // 그 경우 pathToMove 항목을 채운다.

        // init
        ErrorMessage errorMessage = null;

        // 최종 클라이언트에 반환 될 예외 메시지 (JsonObject as String)
        String errorResponse = null;

        // 예외 처리 결과 클라이언트가 이동시킬 페이지 참조 값을 반환해야 하는 경우 에러 코드 범위
        // (300 - 349)
        // 300 : 이미 회원가입한 유저가 회원가입 API 요청한 경우
        // 301 : 회원 절차에 있는 예비 회원정보를 찾을 수 없는 경우
        // 302 : 회원가입 2단계, 3단계 페이지 요청 시, 토큰 유효하지 않거나 토큰 없이 접근한 경우

        // 예외처리 결과 클라이언트가 __페이지를 요청해야 하는 경우
        // 해당 페이지 정보 포함해서 에러 메시지 반환
        if (resultCode >= 300 && resultCode < 350) {
            errorMessage = ErrorMessage
                    .builder()
                    .statusCode(resultCode)
                    .timestamp(new Date())
                    .message(errMsg)
                    .requestPath(requestPath)
                    .pathToMove("/shop/main/index") // 추후 index 페이지 경로 바뀌면 해당 경로 값으로 수정 할 것.
                    .build();
        } else {
            errorMessage = ErrorMessage
                    .builder()
                    .statusCode(resultCode)
                    .timestamp(new Date())
                    .message(errMsg)
                    .requestPath(requestPath)
                    .build();

        }

        Gson gson = new GsonBuilder().create();

        errorResponse = gson.toJson(errorMessage);

        return errorResponse;
    }


}
