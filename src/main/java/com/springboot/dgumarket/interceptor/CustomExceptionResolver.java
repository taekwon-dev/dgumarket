package com.springboot.dgumarket.interceptor;


import com.springboot.dgumarket.exception.notFoundException.ResultNotFoundException;
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
public class CustomExceptionResolver extends AbstractHandlerExceptionResolver {



    @Override
    protected ModelAndView doResolveException(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex) {

        try {
            if (ex instanceof ResultNotFoundException) {
                ModelAndView modelAndView = new ModelAndView();
                modelAndView.setViewName("/exception/error");
                modelAndView.addObject("exception", ex.getMessage());
                return modelAndView;
            }

        } catch (Exception e) {
            log.error("Handling of [" + ex.getClass().getName() + "] resulted in Exception", e);
        }

        return null;
    }


}