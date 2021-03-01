package com.springboot.dgumarket.stomp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.springboot.dgumarket.exception.CustomJwtException;
import com.springboot.dgumarket.exception.ErrorMessage;
import com.springboot.dgumarket.service.UserDetailsServiceImpl;
import com.springboot.dgumarket.utils.JwtUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.util.Date;

/**
 * Created by TK YOUN (2020-11-18 오전 9:31)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */

@Slf4j
@Component
public class CustomStompJwtInterceptor implements ChannelInterceptor {

    private JwtUtils jwtUtils;
    private UserDetailsServiceImpl userDetailsService;


    public CustomStompJwtInterceptor(JwtUtils jwtUtils, UserDetailsServiceImpl userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;

    }

    @SneakyThrows
    // https://partnerjun.tistory.com/55 on @SneakyThrows
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(message);
        if (StompCommand.CONNECT == headerAccessor.getCommand()) { // websocket 연결요청
            log.info("[STOMP]Stomp.Command : CONN");
            String jwtAccessToken = headerAccessor.getFirstNativeHeader("accessToken");
            String jwtRefreshToken = headerAccessor.getFirstNativeHeader("refreshToken");
            String userName = null;


            // Temporary
            if (jwtAccessToken == null) throw new MissingServletRequestParameterException("accessToken", "Cookie");

            userName = jwtUtils.getUsernameFromToken(jwtAccessToken);
            log.info("[STOMP] userName : {}", userName);
            UserDetails userDetails = userDetailsService.loadUserByUsername(userName);

            if (!jwtUtils.validateToken(jwtAccessToken)) {
                log.info("[STOMP] Access Token is not validated, Check Refresh Token");
                if (jwtRefreshToken == null) throw new CustomJwtException(errorResponse());

                if (!jwtUtils.validateToken(jwtRefreshToken)) {
                    log.info("[STOMP] Access, Refresh Token are all not valildated, throw Exception!");
                    throw new CustomJwtException(errorResponse());
                }
                log.info("[STOMP] Access Token is not valildated, but Refresh Token is vaildated");
                return message;

            }
            log.info("[STOMP] Access Token is validated");
            return message;

        } else if (StompCommand.SEND == headerAccessor.getCommand()) {

        }

        return message;
    } // preSend()

    public String errorResponse() {

        ErrorMessage errorMessage = new ErrorMessage(
                HttpStatus.UNAUTHORIZED.value(),
                new Date(),
                "JWT token isn't vaildated, Please login again.",
                "/shop/account/login");

        Gson gson = new GsonBuilder().create();
        String errorResponse = gson.toJson(errorMessage);

        return errorResponse;
    }

}