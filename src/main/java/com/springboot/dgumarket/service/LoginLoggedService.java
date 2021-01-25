package com.springboot.dgumarket.service;

import com.springboot.dgumarket.model.LoggedLogin;
import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.payload.request.LoginRequest;
import com.springboot.dgumarket.repository.member.LoggedLoginRepository;
import com.springboot.dgumarket.utils.CookieUtil;
import com.springboot.dgumarket.utils.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by TK YOUN (2020-11-02 오전 7:39)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 *
 * Member(1) : LoginLogged(n) Relationship
 * 로그인 시점에 유저의 로그인 기록을 남긴다. -> 이 때 토큰 정보를 포함한다.
 *
 * doLogLogin(LoginRequest, HttpServletResponse)
 *
 * 1) LoginRequest -> 로그인 요청 시 전달받은 오브젝트 (웹 메일, 비밀번호, 클라이언트 (브라우저) 체크, IP)
 *
 *    UsernamePasswordAuthenticationToken -> Authentication (인증 처리 진행)
 *    인증 완료 후 -> SecurityContextHolder -> 해당 인증 객체 주입
 *    JWT 토큰 생성 시 해당 Authentication 객체 활용
 *
 * 2) HttpServletResponse -> 생성된 토큰 정보(Access, Refresh Token) 쿠키 생성, 클라이언트 전송
 *
 *
 */
@Service
public class LoginLoggedService {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private CookieUtil cookieUtil;

    private static final Logger logger = LoggerFactory.getLogger(LoginLoggedService.class);

    @Resource
    private LoggedLoginRepository loggedLoginRepository;




    @Transactional
    public void doLogLogin(LoginRequest loginRequest, HttpServletResponse response) {

        /* Authentication(인증 절차 中 UsernamePasswordAuthentication을 사용했고, 인증이 된 경우 -> SecurityContextHolder에 주입  */
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getWebMail(), loginRequest.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String accessToken = jwtUtils.generateToken(userDetails);
        String refreshToken = jwtUtils.generateRefreshToken(userDetails);

        logger.debug("Created Access Token : " + accessToken);
        logger.debug("Created Refresh Token : " + refreshToken);

        /* [JPA Model Relationship - Member(1) & LoginLogged(n)] */
        // member.id -> foreign key로 관계를 맺고 있으므로, 해당 값을 가지고 있는 Member 오브젝트 생성 (Cannot be null)
        Member member = new Member(userDetails.getId());

        LoggedLogin loggedLogin = new LoggedLogin(
                accessToken, refreshToken,"Chrome", "49.247.130.58", 0);

        logger.debug("userDetails.getId() : " + userDetails.getId());

        // addLoginLogging() -> 양방향 관계를 맺고 있는 두 엔티티에 양방향으로 참조할 수 있는 코드 포함되어 있다.
        // [Member] -> 로그인 이력을 추가
        // [LoginLogged] -> 어떤 member에 추가 되었는 지
        member.addLoginLogging(loggedLogin);
        // save()
        loggedLoginRepository.save(loggedLogin);

        // 쿠키 생성 (Access Token, Refresh Token)
        Cookie cookieAccessToken = cookieUtil.createCookie(JwtUtils.ACCESS_TOKEN_NAME, accessToken);
        Cookie cookieRefreshToken = cookieUtil.createCookie(JwtUtils.REFRESH_TOKEN_NAME, refreshToken);

        // 클라이언트 -> 쿠리 리턴
        response.addCookie(cookieAccessToken);
        response.addCookie(cookieRefreshToken);
    }


}
