package com.springboot.dgumarket.model.member;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/** 핸드폰 번호 변경 API 관리를 위한 엔티티 */

@Entity
@Table(name = "findpwd_verification")
@NoArgsConstructor
@Getter
public class FindPwd {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int count;

    // 0 : 비밀번호 재설정 대기, 1 : 비밀번호 재설정 완료
    private int status;

    // 비밀번호 찾기 페이지 접근 토큰
    private String token;

    // 웹메일
    private String webMail;

    // 핸드폰 번호
    private String phoneNumber;

    // 핸드폰 인증 번호(6자리)
    private String phoneVerificationNumber;

    // 인증문자 발송 시간
    private LocalDateTime smsSendDatetime;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createDatetime;

    @UpdateTimestamp
    private LocalDateTime updateDatetime;

    @Builder
    public FindPwd(String webMail, String phoneNumber, String phoneVerificationNumber, LocalDateTime smsSendDatetime) {
        this.webMail = webMail;
        this.phoneNumber = phoneNumber;
        this.phoneVerificationNumber = phoneVerificationNumber;
        this.smsSendDatetime = smsSendDatetime;
    }


    // 1일 5회 제한 계산을 위해 카운트 필드 업데이트
    public void updateCount(int count) {
        this.count = count;
    }

    // 0 : 인증 대기 1 : 인증완료 2 : 비밀번호 재설정 완료
    public void updateStatus(int status) {
        this.status = status;
    }

    // 인증번호 값 업데이트
    public void updatePhoneVerificationNumber(String phoneVerificationNumber) {
        this.phoneVerificationNumber = phoneVerificationNumber;
    }

    public void updateSmsSendDatetime(LocalDateTime smsSendDatetime) {
        this.smsSendDatetime = smsSendDatetime;
    }

    public void updateToken(String token) {
        this.token = token;
    }
}
