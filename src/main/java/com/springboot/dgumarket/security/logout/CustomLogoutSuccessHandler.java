package com.springboot.dgumarket.security.logout;

import com.springboot.dgumarket.repository.member.LoggedLoginRepository;
import com.springboot.dgumarket.utils.CookieUtil;
import com.springboot.dgumarket.utils.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by TK YOUN (2020-11-04 오전 8:23)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 *
 * 로그아웃 요청 (/api/auth/logout) 시, 실행되는 핸들러
 * [로그아웃 요청 클라이언트]가 웹 브라우저이고, 쿠키에 Access Token을 가지고 있다면, 해당 토큰 정보를 블랙리스트에 추가.]
 * (단, 로그아웃을 요청한 클라이언트가 Access Token 값이 없는 경우에는 Redirect 코드 실행
 *      -> DB Cron을 활용해 Refresh Token의 만료기한을 넘긴 경우, 블랙리스트 추가
 *      -> 이를 통해, 중간에 토큰이 탈취되는 문제가 발생하는 경우를 대비할 수 있다.)
 *
 */

@Configuration
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomLogoutSuccessHandler.class);

    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Autowired
    private LoggedLoginRepository loggedLoginRepository;

    @Autowired
    private CookieUtil cookieUtil;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication auth)
            throws IOException, ServletException {

        logger.debug("onLogoutSuccess() is called");

        try {

            if (cookieUtil.getCookie(request, JwtUtils.ACCESS_TOKEN_NAME) != null) {

                final Cookie cookieAccessToken = cookieUtil.getCookie(request, JwtUtils.ACCESS_TOKEN_NAME);
                String accessToken = cookieAccessToken.getValue();
                logger.debug("[Reqeust-Logout] Access Token : " + accessToken);

                // add tokens issued for the user into blacklist. (update db 'auth')
                loggedLoginRepository.addLogoutBlacklist(accessToken);

            }
            else {
                redirectStrategy.sendRedirect(request, response, "/");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // [11-08 ; 로그아웃을 요청하기 직전에 있던 페이지가 권한을 요구하지 않은 경우라면, 해당 페이지로 리턴]
        // [11-08 ; 권한을 요구하는 페이지였다면, 메인화면으로 리턴]
        redirectStrategy.sendRedirect(request, response, "/");
    }
}
