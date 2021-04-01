package com.springboot.dgumarket.interceptor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.springboot.dgumarket.exception.CustomJwtException;
import com.springboot.dgumarket.exception.ErrorMessage;
import com.springboot.dgumarket.exception.JsonParseFailedException;
import com.springboot.dgumarket.exception.aws.AWSProfileImageException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.jni.Error;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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


        if (ex instanceof CustomJwtException) {
            return null;
        }

        if (ex instanceof MissingServletRequestPartException) {

            Map<String, String> resultMap = new HashMap<>();
            resultMap.put("result", "에러 결과 처리");
            ModelAndView modelAndView = new ModelAndView("/error", resultMap);
            modelAndView.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            return modelAndView;
        }


        // return null -> Error Object
        return null;
    }


}