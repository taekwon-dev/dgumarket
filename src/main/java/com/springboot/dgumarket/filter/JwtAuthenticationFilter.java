package com.springboot.dgumarket.filter;

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
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

/**
 * Created by TK YOUN (2021-02-13 오전 12:05)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // 아래 경로 ; 인증 여부에 따른 다른 프로세스 (인증 없이도 인터셉터 통과 가능)
    private static final String API_PRODUCT_INDEX = "/api/product/index";
    private static final String API_SHOP = "(/api/shop/\\d+/)(profile|products|reviews)";

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        log.info("doFilterInternal");

        // init
        String getRequestURI = request.getRequestURI();
        String accessToken = null;
        String username = null;


        if (getRequestURI.equals("/user/login") || getRequestURI.equals("/user/signup")) {
            chain.doFilter(request, response);
            return;
        }

        // NPE
        if (request.getHeader("Authorization") != null) {
            accessToken = request.getHeader("Authorization");
            accessToken = accessToken.split(" ")[1];
            log.info("accessToken : " + accessToken);
        } else {
            // 인증되지 않은 상태에서 요청 (API_PRODUCT_INDEX, API_SHOP)
            // 인터셉터 통과
            if (getRequestURI.equals(API_PRODUCT_INDEX)) chain.doFilter(request, response);;
            if (getRequestURI.matches(API_SHOP)) chain.doFilter(request, response);
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

        chain.doFilter(request, response);
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