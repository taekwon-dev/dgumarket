package com.springboot.dgumarket.utils;


import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by TK YOUN (2020-11-01 오후 1:29)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */
@Slf4j
@Component
public class JwtUtils {


    @Value("${dgumarket.app.jwtSecret}")
    private String SECRET_KEY;

    // 토큰 유효기간 (1주일 - 회원가입 2단계 페이지 접근 시간)
    public final static long TOKEN_VALIDATION_REGISTER_2ND = 1L * 604800 * 1000;

    // 토큰 유효기간 (1시간 - 비밀번호 찾기 페이지 접근 시간)
    public final static long TOKEN_VALIDATION_FIND_PWD = 1L * 3600 * 1000;


    // 회원가입 2단계 페이지 접근 토큰 생성
    public String genTokenForRegister2nd(String webMail) {
        Map<String, Object> claims = new HashMap<>();
        return doGenerateToken(claims, webMail, TOKEN_VALIDATION_REGISTER_2ND);
    }

    // 비밀번호 찾기 페이지 접근 토큰 생성
    public String genTokenForFindPwd(String webMail) {
        Map<String, Object> claims = new HashMap<>();
        return doGenerateToken(claims, webMail, TOKEN_VALIDATION_FIND_PWD);
    }



    //while creating the token -
    //1. Define  claims of the token, like Issuer, Expiration, Subject, and the ID
    //2. Sign the JWT using the HS512 algorithm and secret key.
    //3. According to JWS Compact Serialization(https://tools.ietf.org/html/draft-ietf-jose-json-web-signature-41#section-3.1)
    //   compaction of the JWT to a URL-safe string
    private String doGenerateToken(Map<String, Object> claims, String subject, long exp_time) {
        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + exp_time))
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY).compact();
    }


    //check if the token has expired
    private Boolean isTokenExpired(String token) throws JwtException {
        Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    //retrieve expiration date from jwt token
    public Date getExpirationDateFromToken(String token) throws JwtException {
        return getClaimFromToken(token, Claims::getExpiration);
    }
    //for retrieveing any information from token we will need the secret key
    private Claims getAllClaimsFromToken(String token) throws JwtException {
        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();

    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) throws JwtException {
        try {
            Claims claims = getAllClaimsFromToken(token);
            return claimsResolver.apply(claims);

        }catch (ExpiredJwtException e) {
            // getUsernameFromToken() 과정에서 ExpiredJwtException가 발생하는 경우에도 해당 유저의 이름을 활용해야 하므로
            // 이 곳에서 Catch후 클레임을 상위 호출자에게 전달한다.
            return claimsResolver.apply(e.getClaims());
        }

    }

    public String getUsernameFromToken(String token) throws JwtException {
        return getClaimFromToken(token, Claims::getSubject);
    }


    public Boolean validateToken(String token) throws JwtException {
        return (!isTokenExpired(token));
    }

}

