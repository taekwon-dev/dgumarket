package com.springboot.dgumarket.interceptor;


import com.springboot.dgumarket.exception.notFoundException.ResultNotFoundException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


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
                /** 2021-04-19
                 *  [발견]
                 *  포스트맨 상에서 /api/product/search 요청을 보냈을 때는 이 곳에서 리턴 코드 이후 8080 에러 페이지를 반환했지만,
                 *  웹 브라우저 상에서는 /api/chat/... 이어지는 요청으로 넘어가면서 리턴에서 요청 처리를 중단하지 않는 모습을 보였다.
                 *  ResultNotFoundException을 요청하는 곳의 로그가 찍힌 것까지 확인했고 그 이후 이 부분까지 들어오는 것은 확인
                 *
                 *  ModelAndView modelAndView = new ModelAndView();
                 *  modelAndView.setViewName("/exception/error");
                 *  modelAndView.addObject("exception", ex.getMessage());
                 *  return modelAndView;
                 * */
            }



        } catch (Exception e) {
            log.error("Handling of [" + ex.getClass().getName() + "] resulted in Exception", e);
        }



        return null;
    }


}