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
    public final static long TOKEN_VALIDATION_SECOND = 1L * 604800 * 1000;
    // A 토큰 키 값
    private static String ACCESS_TOKEN_NAME = "accessToken";


    //generate token for user
    public String generateToken(String webMail) {
        Map<String, Object> claims = new HashMap<>();

        return doGenerateToken(claims, webMail);
    }

    //while creating the token -
    //1. Define  claims of the token, like Issuer, Expiration, Subject, and the ID
    //2. Sign the JWT using the HS512 algorithm and secret key.
    //3. According to JWS Compact Serialization(https://tools.ietf.org/html/draft-ietf-jose-json-web-signature-41#section-3.1)
    //   compaction of the JWT to a URL-safe string
    private String doGenerateToken(Map<String, Object> claims, String subject) {

        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + TOKEN_VALIDATION_SECOND))
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY).compact();
    }


    //check if the token has expired
    private Boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    //retrieve expiration date from jwt token
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }
    //for retrieveing any information from token we will need the secret key
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();

    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            return claimsResolver.apply(claims);

        } catch (MalformedJwtException e) {

        } catch (ExpiredJwtException e) {
            return claimsResolver.apply(e.getClaims());
        } catch (UnsupportedJwtException e) {

        } catch (IllegalArgumentException e) {

        }
        return null;
    }

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }


    public Boolean validateToken(String token) {
        return (!isTokenExpired(token));
    }

}