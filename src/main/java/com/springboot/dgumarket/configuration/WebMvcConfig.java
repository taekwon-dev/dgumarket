package com.springboot.dgumarket.configuration;

import com.springboot.dgumarket.interceptor.JwtExceptionResolver;
import com.springboot.dgumarket.interceptor.JwtInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerExceptionResolver;
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
            {"/api/product/**", "/product/**", "/user/*/**", "/user/purchase/**", "/block/**", "/unblock/*", "/report",
            "/chat/**", "/chatroom/**", "/user/profile/**"};

    private JwtInterceptor jwtInterceptor;
    private JwtExceptionResolver jwtExceptionResolver;

    public WebMvcConfig(JwtInterceptor jwtInterceptor, JwtExceptionResolver jwtExceptionResolver) {
        this.jwtInterceptor = jwtInterceptor;
        this.jwtExceptionResolver = jwtExceptionResolver;
    }



    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns(patterns);
    }

    // https://trello.com/c/iNlacAg7/148-dgumarket-restapi-http-exception-handling
    @Override
    public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
        HandlerExceptionResolver exceptionHandlerExceptionResolver = resolvers.stream().filter(x -> x instanceof ExceptionHandlerExceptionResolver).findAny().get();
        int index = resolvers.indexOf(exceptionHandlerExceptionResolver);
        resolvers.add(index, jwtExceptionResolver);
        WebMvcConfigurer.super.extendHandlerExceptionResolvers(resolvers);
    }




    public void addViewControllers(ViewControllerRegistry registry) {
        // 웹 소켓 테스트 페이지 지정 (http://localhost:8080) 으로 접속하면 해당 페이지 로드
        registry.addViewController("/").setViewName("websocket_test");
        registry.addViewController("/ms").setViewName("websocket_test_ms");
        registry.addViewController("/shop/component/nav").setViewName("shop/component/nav");
        registry.addViewController("/shop/component/chat").setViewName("shop/component/chat");
        registry.addViewController("/shop/component/footer").setViewName("shop/component/footer");
        registry.addViewController("/shop/main/index").setViewName("shop/main/index");
        registry.addViewController("/shop/account/find_pwd_certification").setViewName("shop/account/find_pwd_certification");
        registry.addViewController("/shop/account/find_pwd_newPwd").setViewName("shop/account/find_pwd_newPwd");
        registry.addViewController("/shop/account/find_pwd_userInfo").setViewName("shop/account/find_pwd_userInfo");
        registry.addViewController("/shop/account/login").setViewName("shop/account/login");
        registry.addViewController("/shop/account/member_modify").setViewName("shop/account/member_modify");
        registry.addViewController("/shop/account/smartPhone_certification").setViewName("shop/account/smartPhone_certification");
        registry.addViewController("/shop/account/userInfo_input").setViewName("shop/account/userInfo_input");
        registry.addViewController("/shop/account/webMail_certification").setViewName("shop/account/webMail_certification");
        registry.addViewController("/shop/account/change_Pwd").setViewName("shop/account/change_Pwd");
        registry.addViewController("/shop/account/change_smartPhone_number").setViewName("shop/account/change_smartPhone_number");
        registry.addViewController("/shop/account/select_userInfo").setViewName("shop/account/select_userInfo");
        registry.addViewController("/shop/item/myItem").setViewName("shop/item/myItem");
        registry.addViewController("/shop/item/onePick").setViewName("shop/item/onePick");
        registry.addViewController("/shop/item/upLoad").setViewName("shop/item/upLoad");
        registry.addViewController("/shop/{userId:\\d+}/products").setViewName("shop/item/myItem"); // 유저 판매물건 페이지
        registry.addViewController("/shop/{userId:\\d+}/reviews").setViewName("shop/item/myItem"); // 유저 리뷰보기 페이지(유저에게 남긴 리뷰들)
        registry.addViewController("/shop/purchase").setViewName("shop/item/myItem"); // 유저 구매물건 페이지 (인증필)
        registry.addViewController("/product/{productId:\\d+}").setViewName("shop/item/onePick"); // 개별 물건페이지
        registry.addViewController("/category/{categoryId}").setViewName("shop/item/ListbyCondition"); // 카테고리 페이지
    }
}
