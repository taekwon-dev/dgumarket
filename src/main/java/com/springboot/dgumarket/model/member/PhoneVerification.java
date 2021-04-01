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
@Table(name = "phone_verification")
@NoArgsConstructor
@Getter
public class PhoneVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int count;

    // 0 : 인증완료, 1 : 인증대기
    private int status;

    // 핸드폰 번호
    private String phoneNumber;

    // 핸드폰 인증 번호(6자리)
    private String phoneVerificationNumber;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createDatetime;

    @UpdateTimestamp
    private LocalDateTime updateDatetime;

    // 최초 핸드폰 번호 변경 요청 시점
    @Builder
    public PhoneVerification(int count, int status,String phoneNumber, String phoneVerificationNumber) {
        this.count = count;
        this.status = status;
        this.phoneNumber = phoneNumber;
        this.phoneVerificationNumber = phoneVerificationNumber;
    }

    // 1일 5회 제한 계산을 위해 카운트 필드 업데이트
    public void updateCount(int count) {
        this.count = count;
    }

    // 0 : 인증대기 1: 인증완료
    // 핸드폰 인증번호에 대한 각 로우의 상태 (플래그 값 업데이트)
    public void updateStatus(int status) {
        this.status = status;
    }

    // 동일한 핸드폰번호에 대한 인증문자 갱신처리를 위한
    // 인증번호 값 업데이트
    public void updatePhoneVerificationNumber(String phoneVerificationNumber) {
        this.phoneVerificationNumber = phoneVerificationNumber;
    }
}
