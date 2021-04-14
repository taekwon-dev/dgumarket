package com.springboot.dgumarket.utils;

import com.springboot.dgumarket.exception.CustomControllerExecption;
import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.repository.member.MemberRepository;
import com.springboot.dgumarket.service.UserDetailsImpl;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Slf4j
@Component
@Aspect
public class GlobalUserAOP {
    @Autowired
    MemberRepository memberRepository;

    @Around("@annotation(GlobalUserValidate)")
    public Object checkGlobalUserValidate(ProceedingJoinPoint joinPoint) throws Throwable, CustomControllerExecption{
        String parameterName;
        Object[] parameterValues = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        for (int i = 0; i < parameterValues.length; i++) {
            parameterName = method.getParameters()[i].getName();
            if(parameterName.equals("authentication")){
                Authentication authentication = (Authentication)parameterValues[i];
                UserDetailsImpl userDetails = (UserDetailsImpl)authentication.getPrincipal();
                Member member = memberRepository.findById(userDetails.getId());
                if(member==null || member.getIsWithdrawn()==1){throw new CustomControllerExecption("존재하지 않는 유저 입니다.", HttpStatus.NOT_FOUND, null);}
                if(member.getIsEnabled()==1){throw new CustomControllerExecption("관리자로부터 제재조치를 받고 있습니다. 서비스 이용불가", HttpStatus.NOT_FOUND, null);}
            }
        }


        return joinPoint.proceed();
    }
}
