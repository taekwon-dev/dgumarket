package com.springboot.dgumarket.interceptor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.springboot.dgumarket.exception.CustomJwtException;
import com.springboot.dgumarket.exception.ErrorMessage;
import com.springboot.dgumarket.service.UserDetailsServiceImpl;
import com.springboot.dgumarket.utils.JwtUtils;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * Created by TK YOUN (2021-02-10 오전 11:11)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */

/**
 *  1. Access to Authorization-Header o
 *  2. Authenticate
 *   2-1. Exceptions (redirect to the login page)
 *   2-2. Has a difference login with authentication
 *     2-2-1 api/product, api/shop
 */

@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {

    // 아래 경로 ; 인증 여부에 따른 다른 프로세스 (인증 없이도 인터셉터 통과 가능)

    // 카테고리
    private static final String API_PRODUCT_INDEX = "/api/category/index"; // 인기,관심 카테고리 물건들 조회
    private static final String API_PRODUCT_CATEGORY = "/api/category/\\d+/products"; // 카테고리 별 물건들 조회
    // 물건
    private static final String API_PRODUCT_INFO = "/api/product/\\d+/info"; // 물건 조회
    private static final String API_PRODUCT_TOTAL = "/api/product/all"; // 물건들 조회
    // 유저 샵
    private static final String API_SHOP = "/api(/user/\\d+/)(shop-profile|products|reviews)"; // 유저샵프로필/유저샵판매물건/유저샵리뷰

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        log.info("New Jwt Interceptor");

        // init
        String getRequestURI = request.getRequestURI();
        String accessToken = null;
        String username = null;

        // NPE
        if (request.getHeader("Authorization") != null) {
            accessToken = request.getHeader("Authorization");
            accessToken = accessToken.split(" ")[1];
            log.info("accessToken : " + accessToken);
        } else {
            // 인증되지 않은 상태에서 요청 (API_PRODUCT_INDEX, API_SHOP, API_PRODUCT_CATEGORY, API_PRODUCT_INFO, API_PRODUCT_TOTAL)
            // 인터셉터 통과
            if (getRequestURI.equals(API_PRODUCT_INDEX)) return true;
            if (getRequestURI.matches(API_SHOP)) return true;
            if (getRequestURI.matches(API_PRODUCT_CATEGORY)) return true;
            if (getRequestURI.matches(API_PRODUCT_INFO)) return true;
            if (getRequestURI.equals(API_PRODUCT_TOTAL)) return true;

            // cookie (리프레시 토큰) 삭제
            removeRefreshCookie(response);
            // 위 경로 제외하고는 인증이 요구되는 API에서 인증 헤더가 누락된 경우 예외를 발생.
            throw new JwtException(errorResponse("인증 검사를 위한 Authorization Header가 누락된 경우",304,"특정할 수 없습니다."));
        }

        if (!jwtUtils.validateToken(accessToken)) {
            // cookie (리프레시 토큰) 삭제
            removeRefreshCookie(response);
            throw new JwtException(errorResponse("Dgumarket 서버 - A 토큰이 유효하지 않은 경우", 304, "특정할 수 없습니다."));
        }
        // get the username from the jwt claims
        username = jwtUtils.getUsernameFromToken(accessToken);


         // init userDetails via the username
        try {
            // loadUserByUsername() 에서 예외가 발생하지 않는다면, try-catch 블록 다음에 있는 true 실행
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // authenticate via the access token
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
            usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

        } catch (UsernameNotFoundException e) {
            // UsernameNotFoundException
            // 1) 쿠키에 저장되어 있는 리프레시 토큰 삭제
            // 2) throw new UsernameNotFoundException()
            removeRefreshCookie(response);
            throw new UsernameNotFoundException(errorResponse("API 요청한 유저의 고유 ID로 회원을 식별할 수 없는 경우 ", 303, "(UserDetailsService)특정할 수 없습니다."));

        }
        return true;
    }

    private String errorResponse(String errMsg, int resultCode, String requestPath) {

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
        // (300 - 319)
        // 300 : 이미 회원가입한 유저가 회원가입 API 요청한 경우
        // 301 : 회원 절차에 있는 예비 회원정보를 찾을 수 없는 경우
        // 302 : 회원가입 2단계, 3단계 페이지 요청 시, 토큰 유효하지 않거나 토큰 없이 접근한 경우

        // 예외처리 결과 클라이언트가 __페이지를 요청해야 하는 경우
        // 해당 페이지 정보 포함해서 에러 메시지 반환
        if (resultCode >= 300 && resultCode < 320) {
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

    private void removeRefreshCookie(HttpServletResponse response) {
        // R 토큰 쿠키 삭제
        // create a cookie
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);

        // https 적용 상황에서 주석 풀고 테스트 하기.
        // cookie.setSecure(true);
        cookie.setMaxAge(0);

        // add cookie to response
        response.addCookie(cookie);
    }

}
