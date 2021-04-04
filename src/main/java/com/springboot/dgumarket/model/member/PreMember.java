package com.springboot.dgumarket.model.member;


import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "pre_members")
@NoArgsConstructor
@Getter
public class PreMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // 웹메일
    @NotBlank
    private String webMail;

    // 웹메일에 1:1 대응되는 고유 식별값 (유효기간(7일)을 가지고 있다)
    @NotBlank
    private String webmailJwt;

    // 핸드폰 번호 인증 문자 1일 최대 5회 제한 처리를 위한 count
    private int count;

    // 인증문자 발송 시간
    private LocalDateTime smsSendDatetime;

    // 웹메일을 통해 가입한 회원 여부를 식별 (0 : 비회원, 1 : 회원, 2: 탈퇴 회원)
    // default : 0 (= 최초 웹메일 인증 API 요청 시점에는 비회원 상태이므로)
    private int status;

    // 핸드폰 번호
    private String phoneNumber;

    // 핸드폰 인증 번호 (= 핸드폰 인증 문자 발송 API로 유저에게 발송된 인증 번호)
    private String phoneVerificationNumber;

    // 입력한 '웹메일'로 최초 웹메일 인증 API를 요청한 시점
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createDatetime;

    // 웹메일 인증 API 또는 핸드폰 인증 과정에서 일부 컬럼이 마지막으로 업데이트 된 시점
    @UpdateTimestamp
    private LocalDateTime updateDatetime;



    // 웹메일 인증 API 요청 시 웹메일과 해당 웹메일에 1:1 대응되는 고유 식별값 객체 생성 -> save() or init()
    @Builder
    public PreMember(String webMail, String webmailJwt) {
        this.webMail = webMail;
        this.webmailJwt = webmailJwt;
        this.status = 0;
    }

    // 기존 웹메일 인증 API 요청 내역이 있는 경우
    // 기존 고유 식별값 업데이트
    // (필요에 따라) 웹메일 인증 API 요청 시 핸드폰 번호, 핸드폰 인증 번호에 대한 초기화(null) 처리


    public void updateWebMailJwt(String webmailJwt) {
        this.webmailJwt = webmailJwt;
    }

    public void updatePhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void updatePhoneVerificationNumber(String phoneVerificationNumber) {
        this.phoneVerificationNumber = phoneVerificationNumber;
    }

    public void updateSmsSendDatetime(LocalDateTime smsSendDatetime) {
        this.smsSendDatetime = smsSendDatetime;
    }

    public void updatePreMemberStatus(int status) {
        this.status = status;
    }

    public void updateCount(int count) {
        this.count = count;
    }

    public void initPhoneNumber() {
        this.phoneNumber = null;
    }

    public void initPhoneVerificationNumber() {
        this.phoneVerificationNumber = null;
    }

    public void initCount() {
        this.count = 1;
    }


}
