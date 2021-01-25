package com.springboot.dgumarket.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.springboot.dgumarket.model.member.Member;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Created by TK YOUN (2020-10-20 오전 8:17)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */

@Entity
@Table(name = "logged_logins")
@Getter
@Setter
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class LoggedLogin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // (JWT) Access Token
    @Size(max = 1024)
    private String accessToken;

    // (JWT) Refresh Token
    @Size(max = 1024)
    private String refreshToken;

    // (접속) 브라우저
    private String browser;

    // (접속) 호스트 (IP)
    private String host;

    // 상태 값 (0 - vaildate, 1 - blacklist (expired or logged-out))
    private int status;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createDatetime;

    @UpdateTimestamp
    private LocalDateTime updateDatetime;

    /* Relationship */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;


    public LoggedLogin(String accessToken, String refreshToken, String browser, String host, int status) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.browser = browser;
        this.host = host;
        this.status = status;
    }





}
