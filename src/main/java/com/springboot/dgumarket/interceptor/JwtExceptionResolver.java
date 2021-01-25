package com.springboot.dgumarket.interceptor;

import com.springboot.dgumarket.exception.CustomJwtException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by TK YOUN (2020-12-03 오전 1:37)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */
@Slf4j
@Component
public class JwtExceptionResolver extends AbstractHandlerExceptionResolver {

    @Override
    protected ModelAndView doResolveException(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex) {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("shop/account/login");
        modelAndView.addObject("exception", ex.getMessage());
        try {
            if (ex instanceof CustomJwtException) {
                log.error("[JWT ExpiredJwtException]");

                return modelAndView;
            }

        } catch (Exception handlerException) {
            log.warn("Handling of [" + ex.getClass().getName() + "] resulted in Exception", handlerException);
        }

        log.error("exceptions : {}" + ex.getMessage() );
        return null;
    }


}