package com.springboot.dgumarket.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Optional;

/**
 * Created by TK YOUN (2020-11-01 오후 1:29)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */
@Component
public class CookieUtil {

    private static final Logger logger = LoggerFactory.getLogger(CookieUtil.class);


    public Cookie createCookie(String cookieName, String value) {


        Cookie token = new Cookie(cookieName, value);
        token.setHttpOnly(false); // WebSocket CONN, SEND 시점에 JS로 쿠키에 접근할 수 없는 문제 (추후에 다른 방법으로 토큰을 전달할 수 있는 것을 찾아야 함)
        token.setMaxAge(60 * 60 * 24 * 5);
        token.setPath("/");
        return token;
    }

    public Cookie getCookie(HttpServletRequest req, String cookieName) {

        logger.debug("getCookie() is called with cookieName : " + cookieName);
        final Cookie[] cookies = req.getCookies();

        if (cookies == null) {
            logger.debug("cookie is null");
            return null;
        }

        for (Cookie cookie : cookies) {

            if (cookie.getName().equals(cookieName)) {
                logger.debug("cookieName : " + cookieName);
                return cookie;
            }

        }


        return null;
    }

}