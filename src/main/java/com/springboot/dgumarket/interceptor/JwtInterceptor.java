package com.springboot.dgumarket.interceptor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.springboot.dgumarket.exception.CustomJwtException;
import com.springboot.dgumarket.exception.ErrorMessage;
import com.springboot.dgumarket.service.UserDetailsServiceImpl;
import com.springboot.dgumarket.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

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
    private static final String API_PRODUCT_INDEX = "/api/product/index";
    private static final String API_SHOP = "(/user/\\d+/)(shop-profile|products|reviews)"; // 유저샵프로필/유저샵판매물건/유저샵리뷰
    private static final String API_PRODUCT_CATEGORY = "/category/\\d+"; // 카테고리별 물건보여주기
    private static final String API_PRODUCT_TOTAL = "/products"; // 전체물건보여주기
    private static final String API_PRODUCT_INFO = "/api/product/\\d+/info"; // 개별물건보여주기 (선택)

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
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
            // 특정 경로 외, 예외
            throw new CustomJwtException(errorResponse(request, "There is no access token for authentication"));
        }

        // Exceptions : if the access token (jwt) is not validated
        if (!jwtUtils.validateToken(accessToken)) throw new CustomJwtException(errorResponse(request, "The access token is not validated on dgumarket"));

        // get the username from the jwt claims
        username = jwtUtils.getUsernameFromToken(accessToken);

        // init userDetails via the username
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // authenticate via the access token
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
        usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

        return true;
    }

    public String errorResponse(HttpServletRequest request, String msg) {

        ErrorMessage errorMessage = new ErrorMessage(
                HttpStatus.UNAUTHORIZED.value(),
                new Date(),
                msg,
                request.getRequestURI());

        Gson gson = new GsonBuilder().create();
        String errorResponse = gson.toJson(errorMessage);

        return errorResponse;
    }
}
