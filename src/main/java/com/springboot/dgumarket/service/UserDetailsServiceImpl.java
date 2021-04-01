package com.springboot.dgumarket.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.springboot.dgumarket.exception.ErrorMessage;
import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.repository.member.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * Created by TK YOUN (2020-11-01 오후 1:40)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private MemberRepository memberRepository;


    // throws 예약어 역할 :
    // throws 예약어를 통해 loadUserByUsername() 메서드를 호출한 상위 메서드로 예외를 발생시킨다.
    // 예외처리를 '전가'시키는 것이다.

    // 단, Optional 사용으로, 'orElseThrow()' 키워드가 있는 경우 (경험적 발견)
    // loadUserByUsername() 메서드를 호출한 상위 메서드에 예외 처리를 하지 않은 경우 이곳에서 예외처리를 한다. (전가시키지 않는다)


    @Transactional
    @Override
    public UserDetails loadUserByUsername(String webMail) throws UsernameNotFoundException {

        Member member = memberRepository.findByWebMailAndIsWithdrawn(webMail, 0)
                // UsernameNotFoundException extends AuhenticationException -> AuthEntryPointJwt.class
                .orElseThrow(()-> new UsernameNotFoundException(errorResponse("API 요청한 유저의 고유 ID로 회원을 식별할 수 없는 경우 ", 303, "(UserDetailsService)특정할 수 없습니다.")));

        return UserDetailsImpl.build(member);
    }

    // 유저 프로필 관련 API 예외 메시지 오브젝트 생성 및 리턴
    public String errorResponse(String errMsg, int resultCode, String requestPath) {

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
}

