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


    @Value("${bezkoder.app.jwtSecret}")
    private String SECRET_KEY;

    public final static long TOKEN_VALIDATION_SECOND = 1L * 500000000 * 1000;
    public final static long REFRESH_TOKEN_VALIDATION_SECOND = 1L * 500000000 * 1000;

    final static public String ACCESS_TOKEN_NAME = "accessToken";
    final static public String REFRESH_TOKEN_NAME = "refreshToken";

    //generate token for user
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        log.info("[JwtUtils] generateToken()");
        return doGenerateToken(claims, userDetails.getUsername());
    }

    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        log.info("[JwtUtils] generateRefreshToken()");
        return doRefreshGenerateToken(claims, userDetails.getUsername());
    }


    //while creating the token -
    //1. Define  claims of the token, like Issuer, Expiration, Subject, and the ID
    //2. Sign the JWT using the HS512 algorithm and secret key.
    //3. According to JWS Compact Serialization(https://tools.ietf.org/html/draft-ietf-jose-json-web-signature-41#section-3.1)
    //   compaction of the JWT to a URL-safe string
    private String doGenerateToken(Map<String, Object> claims, String subject) {
        log.info("[JwtUtils] doGenerateToken()");
        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + TOKEN_VALIDATION_SECOND))
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY).compact();
    }

    private String doRefreshGenerateToken(Map<String, Object> claims, String subject) {
        log.info("[JwtUtils] doRefreshGenerateToken()");
        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_VALIDATION_SECOND))
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY).compact();
    }

    //check if the token has expired
    private Boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        log.info("[JwtUtils] isTokenExpired()");
        return expiration.before(new Date());
    }

    //retrieve expiration date from jwt token
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    //for retrieveing any information from token we will need the secret key
    private Claims getAllClaimsFromToken(String token) {
        log.info("[JwtUtils] getAllClaimsFromToken()");
        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();

    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            log.info("[JwtUtils] getClaimFromToken()");
            return claimsResolver.apply(claims);

        } catch (MalformedJwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.warn("JWT token is expired: {}", e.getMessage());
            return claimsResolver.apply(e.getClaims());
        } catch (UnsupportedJwtException e) {
            log.warn("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty: {}", e.getMessage());
        }

        return null;
    }

    public String getUsernameFromToken(String token) {
        log.info("[JwtUtils] getUsernameFromToken()");
        return getClaimFromToken(token, Claims::getSubject);
    }



    //validate token
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        log.info("[JwtUtils] validateToken()");
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }




}

