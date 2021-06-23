package com.springboot.dgumarket.configuration;

import com.springboot.dgumarket.interceptor.CustomExceptionResolver;
import com.springboot.dgumarket.interceptor.JwtInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;

import java.util.List;

/**
 * Created by TK YOUN (2020-11-04 오후 5:25)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */
@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private final static String[] patterns =
            {
                    // 카테고리 별 물건 정보
                    "/api/category/index", // 인기/관심 카테고리 별 물건보여주기 [ 인증선택, jwt interceptor 예외 추가 ]
                    "/api/category/*/products", // 카테고리별 물건조회 [ 인증선택, jwt interceptor 예외 추가 ]

                    // 물건 상세
                    "/api/product/*/info", // 개별물건정보 [ 인증선택, jwt interceptor 예외 추가]
                    "/api/product/all", // 전체 물건보기 [ 인증선택, jwt interceptor 예외 추가 ]
                    "/api/product/search", // 검색 결과 [ 인증선택, jwt interceptor 예외 추가]
                    "/api/product/like", // 좋아요 및 좋아요 취소하기 [ 인증 필요 ]
                    "/api/product/*/comment", // 구매후기보기(get),남기기(post) [인증 필요]
                    "/api/product/upload", // 상품 업로드
                    "/api/product/modify", // 상품 수정
                    "/api/product/delete",  // 상품 삭제
                    "/api/product/*/chat-history", // 해당 물건에 대해서 채팅으로 거래중인지 확인하는 API

                    // 채팅
                    "/api/chat/**", // 채팅 관련 API [인증 필요]
                    "/api/chatroom/**", // 채팅방 관련 API [인증 필요]

                    // 내 거래정보
                    "/api/user/profile/**", // 유저 API [인증 필요]
                    "/api/send-sms/change-phone", // 유저 핸드폰 변경 시 핸드폰 인증 문자 전송[인증 필요]
                    "/api/user/purchase/**", // 유저샾 구매물건 보기 [인증 필요]
                    "/api/user/*/**", // 유저 차단하기(user/1/shop-profile), 판매물건(/user/1/product), 리뷰 조회(/user/1/reviews) [ 인증선택 jwt interceptor 예외 추가 ]

                    // 기타 기능
                    "/api/block/**", // 유저 차단하기 [인증 필요]
                    "/api/unblock/*", // 유저 차단해제하기 [인증 필요]
                    "/api/report", // 유저 신고하기 [인증 필요]
                    "/api/blocklist", // 유저 차단리스트 조회하기 [인증 필요]
                    // AWS S3 복수 이미지 API
                    "/api/multi-img/**", // 복수 이미지 업로드, 삭제 API, (업로드 + 삭제 로직 모두 포함된 API) (via AWS S3)


                    // [관리자] 기능
                    "/api/admin/reports", // 신고접수건들 조회하기
                    "/api/admin/report-results", // 신고처리결과들(or관리자 처리결과) 조회하기
                    "/api/admin/report/*/result", // [관리자] 신고접수건에 대해서 신고처리
                    "/api/admin/alert/*", // [관리자자체] 경고주기 및 취소
                    "/api/admin/blind/*", // [관리자자체] 물건 블라인드처리 및 취소
                    "/api/admin/sanction/*", // [관리자자체] 유저 제재처리 및 취소
                    "/api/admin/report/*/status" // 신고접수상태 바꾸기
            };

    private JwtInterceptor jwtInterceptor;
    private CustomExceptionResolver customExceptionResolver;

    public WebMvcConfig(JwtInterceptor jwtInterceptor, CustomExceptionResolver customExceptionResolver) {
        this.jwtInterceptor = jwtInterceptor;
        this.customExceptionResolver = customExceptionResolver;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry
                // 모든 URL 패턴에 대해서
                .addMapping("/**")
                // www.dgumarket.co.kr Origin 허용한다.
                .allowedOrigins("https://www.dgumarket.co.kr");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns(patterns)
                // URL 패턴 설계 오류 (중복문제)
                // 인증이 필요 없는 아래 패턴들은 interceptor 제외한다.
                .excludePathPatterns("/api/user/signup", "/api/user/check-webmail", "/api/user/send-webmail", "/api/user/find-pwd/verify-phone", "/api/user/find-pwd", "/api/user/delete-member");
    }

    // https://trello.com/c/iNlacAg7/148-dgumarket-restapi-http-exception-handling
    @Override
    public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
        HandlerExceptionResolver exceptionHandlerExceptionResolver = resolvers.stream().filter(x -> x instanceof ExceptionHandlerExceptionResolver).findAny().get();
        int index = resolvers.indexOf(exceptionHandlerExceptionResolver);
        resolvers.add(index, customExceptionResolver);
        WebMvcConfigurer.super.extendHandlerExceptionResolvers(resolvers);
    }

}
