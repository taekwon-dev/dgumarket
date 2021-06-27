package com.springboot.dgumarket.interceptor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.springboot.dgumarket.exception.ErrorMessage;
import com.springboot.dgumarket.service.UserDetailsServiceImpl;
import com.springboot.dgumarket.utils.JwtUtils;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

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
    private static final String API_PRODUCT_SEARCH = "/api/product/search"; // 검색 결과 조회
    // 유저 샵
    private static final String API_SHOP = "/api(/user/\\d+/)(shop-profile|products|reviews)"; // 유저샵프로필/유저샵판매물건/유저샵리뷰

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        // init
        String getRequestURI = request.getRequestURI();
        String accessToken = null;
        String username = null;

        // 로그인 상태
        if (request.getHeader("Authorization") != null) {

            // 위 경로에 해당하는 경우에 "null" Auth 헤더가 오는 경우 -> 유효하지 않은 A 토큰으로 요청한 경우
            // 유효하지 않은 A 토큰으로 요청했다는 것은 -> 로그인 상태를 전제로 한다.
            // 따라서, 로그인 페이지로 리다이렉트 처리한다.

            // 로그인 상태이지만, Access Token이 유효하지 않은 상태에서 요청하는 경우, 로그인 페이지로 리다이렉트 처리
            // 추후, 정적 자원(페이지 등)이 Dgumarket 서버에서 인증 처리되는 경우로 보완
            if (request.getHeader("Authorization").equals("null")) return true;

            // 위 경로 포함해서 인증이 필요한 API 요청에 한해서 A 토큰의 값이 있는 경우는
            // 이미 SCG 서버에서 토큰 유효성이 통과한 경우
            // 따라서, 이 경우는 이미 유저의 로그인 상태로 전제 가능
            accessToken = request.getHeader("Authorization");
            accessToken = accessToken.split(" ")[1];

        } else {
            // 비로그인 상태에서 요청하는 경우
            // 아래, 비인증 API의 경우, 인터셉터 통과시킨다. (비로그인 상태로 해당 API 응답 반환)
            if (getRequestURI.equals(API_PRODUCT_INDEX)) return true;
            if (getRequestURI.matches(API_SHOP)) return true;
            if (getRequestURI.matches(API_PRODUCT_CATEGORY)) return true;
            if (getRequestURI.matches(API_PRODUCT_INFO)) return true;
            if (getRequestURI.equals(API_PRODUCT_TOTAL)) return true;
            if (getRequestURI.equals(API_PRODUCT_SEARCH)) return true;

            // 인증 대상 API의 경우, 리프레시 토큰을 삭제 시키고, 아래 예외를 발생시킨다.
            // cookie (리프레시 토큰) 삭제
            removeRefreshCookie(response);
            // 위 경로 제외하고는 인증이 요구되는 API에서 인증 헤더가 누락된 경우 예외를 발생.
            // 현재 구조에서는 Gateway 서버에서 1차 필터 역할을 함으로, 이 부분이 발생할 가능성은 없다.
            throw new JwtException(errorResponse("인증 검사를 위한 Authorization Header가 누락된 경우",304,"특정할 수 없습니다."));
        }

        if (!jwtUtils.validateToken(accessToken)) {
            // ExpiredJwtException 예외가 발생헀을 때, 클레임 정보를 리턴하도록 예외처리가 됐으므로,
            // 아래 쿠키 삭제하는 로직과, JwtException Throwing 하는 로직이 순서대로 동작한다.

            // cookie (리프레시 토큰) 삭제
            removeRefreshCookie(response);
            // 현재 구조에서는 Gateway 서버에서 1차 필터 역할을 함으로, 이 부분이 발생할 가능성은 없다.
            throw new JwtException(errorResponse("Dgumarket 서버 - A 토큰이 유효하지 않은 경우", 304, "특정할 수 없습니다."));
        }

        // JWT 토큰 이슈에서 ExpiredJwtException 상황을 배제할 수 있다.
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
            throw new UsernameNotFoundException(errorResponse("Dgumarket 서버, (탈퇴 또는 이용제재)로그인 상태 + 비인증 페이지 요청하는 경우 로그인 페이지로 리다이렉트", 355,  "/shop/account/login"));

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
                    .pathToMove("/") // 추후 index 페이지 경로 바뀌면 해당 경로 값으로 수정 할 것.
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
