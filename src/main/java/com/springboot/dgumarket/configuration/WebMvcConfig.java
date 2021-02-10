package com.springboot.dgumarket.configuration;

import com.springboot.dgumarket.interceptor.JwtExceptionResolver;
import com.springboot.dgumarket.interceptor.JwtInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;

import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;

/**
 * Created by TK YOUN (2020-11-04 오후 5:25)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */
@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private JwtInterceptor jwtInterceptor;
    private JwtExceptionResolver jwtExceptionResolver;

    public WebMvcConfig(JwtInterceptor jwtInterceptor, JwtExceptionResolver jwtExceptionResolver) {
        this.jwtInterceptor = jwtInterceptor;
        this.jwtExceptionResolver = jwtExceptionResolver;
    }



    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/product/**")
                .addPathPatterns("/api/shop/*/**")
                .addPathPatterns("/user/block/**")
                .addPathPatterns("/report")
                .addPathPatterns("/chat/**")
                .addPathPatterns("/user/profile/**");
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
        registry.addViewController("/shop/item/category").setViewName("shop/item/category");
        registry.addViewController("/shop/item/ListbyCondition").setViewName("shop/item/ListbyCondition");
        registry.addViewController("/shop/item/myItem").setViewName("shop/item/myItem");
        registry.addViewController("/shop/item/onePick").setViewName("shop/item/onePick");
        registry.addViewController("/shop/item/upLoad").setViewName("shop/item/upLoad");
        registry.addViewController("/shop/{userId:\\d+}/products").setViewName("shop/item/myItem"); // 본격적으로 추가되는 view url
        registry.addViewController("/shop/{userId:\\d+}/reviews").setViewName("shop/item/myItem");
        registry.addViewController("/shop/{userId:\\d+}/purchase").setViewName("shop/item/myItem");
        registry.addViewController("/shop/product/{productId:\\d+}").setViewName("shop/item/onePick");
    }
}
