package com.springboot.dgumarket.model.member;


import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.persistence.*;
import java.time.LocalDateTime;


/**
 * Created by TK YOUN (2021-02-13 오후 8:18)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */
@Entity
@Table(name = "users")
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // 유저 웹 메일
    @NotBlank
    private String webMail;

    // 유저 핸드폰 번호
    @NotBlank
    private String phoneNumber;

    // 유저 닉네임
    @NotBlank
    @Size(max = 20)
    private String nickName;

    // 비밀번호
    @NotBlank
    @Size(max = 60)
    private String password;

    // 프로필 이미지 경로
    private String profileImageDir;

    // 회원 - 이용제한 여부
    private int isEnabled;

    // 회원 - 탈퇴 여부
    private int isWithdrawn;

    // 권한
    private String roles;

    // 회원 가입 일시
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createDatetime;

    // 회원 정보 마지막 수정 일시 (회원정보 수정 또는 회원 탈퇴)
    @UpdateTimestamp
    private LocalDateTime updateDatetime;

    // [회원탈퇴 API]
    // 회원탈퇴 요청 시 회원 탈퇴 상태 값을 1로 수정한다. (일정 기간 보호 후 삭제)
    public void updateUserStatus(int isWithdrawn) {
        this.isWithdrawn = isWithdrawn;
    }

    // 유저 제재가하기
    public void punish(){
        this.isEnabled = 1;
    }

    // 유저 제재 취소하기
    public void unPunish(){
        this.isEnabled = 0;
    }
}
