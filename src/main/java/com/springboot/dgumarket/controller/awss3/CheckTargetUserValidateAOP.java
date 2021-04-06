package com.springboot.dgumarket.controller.awss3;

import com.springboot.dgumarket.exception.CustomControllerExecption;
import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.repository.member.MemberRepository;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Optional;

@Component
@Aspect
public class CheckTargetUserValidateAOP {

    @Autowired
    MemberRepository memberRepository;

    @Pointcut("@annotation(CheckTargetUserValidate)")
    public void upLoadImagefile(){}


    @Before(value = "upLoadImagefile()")
    public void checkTargetUser(JoinPoint joinPoint) throws CustomControllerExecption {
        System.out.println("CheckTargetUserValidateAOP.checkTargetUser : aop 발동");
        String parameterName;
        Object[] parameterValues = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        for (int i = 0; i < parameterValues.length; i++) {
            parameterName = method.getParameters()[i].getName();
            if (parameterName.equals("targetId")) { //  채팅이미지 업로드 요청일 경우
                System.out.println("aop targetId detect!");
                Integer targetUserId = (Integer)parameterValues[i];
                Optional<Member> member = memberRepository.findById(targetUserId);
                if(member.isPresent()){
                    if(member.get().getIsWithdrawn()==1){throw new CustomControllerExecption("탈퇴한 유저에게 채팅 이미지를 전송할 수 없습니다.", HttpStatus.NOT_FOUND);}
                    if(member.get().getIsEnabled()==1){throw new CustomControllerExecption("관리자로 부터 이용제재 받고 있는 유저에게 채팅 이미지를 전송할 수 없습니다.", HttpStatus.NOT_FOUND);}
                }else{
                    throw new CustomControllerExecption("존재하지 않는 유저에게 채팅 이미지를 전송할 수 없습니다.", HttpStatus.NOT_FOUND);
                }
            }

            if(parameterName.equals("authentication")){
                System.out.println("aop authentication!");
            }
        }
    }
}
