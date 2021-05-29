package com.springboot.dgumarket.controller.member;


import com.springboot.dgumarket.dto.member.FindPwdDto;
import com.springboot.dgumarket.dto.member.ResetPwdDto;
import com.springboot.dgumarket.dto.member.SignUpDto;

import com.springboot.dgumarket.model.chat.ChatMessage;
import com.springboot.dgumarket.model.chat.ChatRoom;
import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.payload.request.WebmailRequest;

import com.springboot.dgumarket.payload.response.ApiResultEntity;
import com.springboot.dgumarket.repository.member.BlockUserRepository;
import com.springboot.dgumarket.repository.member.MemberRepository;
import com.springboot.dgumarket.service.mail.EmailService;

import com.springboot.dgumarket.service.member.MemberProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
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

        // param : 받는 사람의 이메일 (동국대학교 웹메일)
        emailService.send(webmailRequest.getWebMail());


        ApiResultEntity apiResponseEntity = ApiResultEntity.builder()
                .statusCode(1)
                .message("인증메일을 발송했습니다.")
                .responseData(null)
                .build();

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



}
