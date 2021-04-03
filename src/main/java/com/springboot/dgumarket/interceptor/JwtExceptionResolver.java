package com.springboot.dgumarket.interceptor;

import com.springboot.dgumarket.exception.CustomJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
        modelAndView.setViewName("/shop/account/login");
        modelAndView.addObject("exception", ex.getMessage());
        try {
            if (ex instanceof CustomJwtException) {
                log.error("[JWT ExpiredJwtException]");
                return modelAndView;
            }

            if (ex instanceof IncorrectResultSizeDataAccessException) {
                // Error 페이지 반환
                // 이전 입력 했던 내용 불러오기 기능 부탁
                log.error("IncorrectResultSizeDataAccessException");
            }

        } catch (Exception handlerException) {
            log.error("Handling of [" + ex.getClass().getName() + "] resulted in Exception", handlerException);
        }

        // return null -> Error Object
        return null;
    }


}