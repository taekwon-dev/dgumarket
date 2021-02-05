package com.springboot.dgumarket.interceptor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.springboot.dgumarket.exception.CustomJwtException;
import com.springboot.dgumarket.exception.ErrorMessage;
import com.springboot.dgumarket.model.LoggedLogin;
import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.repository.member.LoggedLoginRepository;
import com.springboot.dgumarket.service.UserDetailsImpl;
import com.springboot.dgumarket.service.UserDetailsServiceImpl;
import com.springboot.dgumarket.utils.CookieUtil;
import com.springboot.dgumarket.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * Created by TK YOUN (2020-11-27 오후 9:15)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */
@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(JwtInterceptor.class);
    private static final String API_PRODUCT_INDEX = "/api/product/index";
    private static final String API_SHOP = "(/api/shop/\\d+/)(profile|products|reviews)"; // jwt 없을 경우도 pass 가능

    private CookieUtil cookieUtil;
    private JwtUtils jwtUtils;
    private UserDetailsServiceImpl userDetailsService;
    private LoggedLoginRepository loggedLoginRepository;

    public JwtInterceptor(JwtUtils jwtUtils, CookieUtil cookieUtil, UserDetailsServiceImpl userDetailsService, LoggedLoginRepository loggedLoginRepository) {
        this.jwtUtils = jwtUtils;
        this.cookieUtil = cookieUtil;
        this.userDetailsService = userDetailsService;
        this.loggedLoginRepository = loggedLoginRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {


        String getRequestURI = request.getRequestURI();
        Cookie cookieAccessToken = cookieUtil.getCookie(request, "accessToken");

        String username = null;
        String accessToken = null;

        String refreshToken = null;

        if (cookieAccessToken != null) {

            accessToken = cookieAccessToken.getValue();
            logger.info("[1]Access Token : {}", accessToken);

            // if there is a exception in the middle of parsing usrename, this method would throw Exception.
            username = jwtUtils.getUsernameFromToken(accessToken);
            logger.info("[2] getUsernameFromToken : {}", username);

            // if there is a exception in the middle of loading usrename, this method would throw Exception.
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            logger.info("[3]vRight before JWT vaildate check");

            if (!jwtUtils.validateToken(accessToken, userDetails)) {
                logger.info("[3-1] Access Token is not validated");
                // Refresh Token 검증
                Cookie cookieRefreshToken = cookieUtil.getCookie(request, JwtUtils.REFRESH_TOKEN_NAME);

                // If the Refresh Token dosen't exist, throw Exception.
                if (cookieRefreshToken == null) throw new CustomJwtException(errorResponse(request));
                refreshToken = cookieRefreshToken.getValue();
                logger.info("Refresh Token : {}", refreshToken);

                if (!jwtUtils.validateToken(refreshToken, userDetails)) {
                    log.info("[STOMP] Access, Refresh Token are all not valildated, throw Exception!");
                    loggedLoginRepository.addBlacklist(loggedLoginRepository.findMemberIdbyRefreshToken(refreshToken), refreshToken);
                    // Access Token, Refresh Token are all not vaildated, throw Exception
                    throw new CustomJwtException(errorResponse(request));
                }

                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
                usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

                String renewAccessToken = jwtUtils.generateToken(userDetails);
                String renewRefreshToken = jwtUtils.generateRefreshToken(userDetails);

                Cookie cookieNewAccessToken = cookieUtil.createCookie(JwtUtils.ACCESS_TOKEN_NAME, renewAccessToken);
                Cookie cookieNewRefreshToken = cookieUtil.createCookie(JwtUtils.REFRESH_TOKEN_NAME, renewRefreshToken);

                response.addCookie(cookieNewAccessToken);
                response.addCookie(cookieNewRefreshToken);

                UserDetailsImpl userDetailsForUpdateToken = (UserDetailsImpl) usernamePasswordAuthenticationToken.getPrincipal();
                Member member = new Member(userDetailsForUpdateToken.getId());

                LoggedLogin loggedLogin = new LoggedLogin(
                        renewAccessToken, renewRefreshToken,  "T_Chrome", "94.247.130.58", 0
                );

                // 기존의 Refresh 토큰이 포함된 행을 블랙리스트에 추가. (이전 토큰을 통해 로그인 할 수 없도록)
                loggedLoginRepository.addBlacklist(userDetailsForUpdateToken.getId(), refreshToken);

                member.addLoginLogging(loggedLogin);
                loggedLoginRepository.save(loggedLogin);
                return true;
            }

            logger.info("[3-2] Access Token is validated");
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
            usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            return true;

        } else {
            if (getRequestURI.equals(API_PRODUCT_INDEX)) return true;
            if (getRequestURI.matches(API_SHOP)) return true;
            throw new CustomJwtException(errorResponse(request));
        }
    }

    public String errorResponse(HttpServletRequest request) {

        ErrorMessage errorMessage = new ErrorMessage(
                HttpStatus.UNAUTHORIZED.value(),
                new Date(),
                "JWT token isn't vaildated, Please login again.",
                request.getRequestURI());

        Gson gson = new GsonBuilder().create();
        String errorResponse = gson.toJson(errorMessage);

        return errorResponse;
    }

}
