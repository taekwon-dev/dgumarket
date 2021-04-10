package com.springboot.dgumarket.controller.awss3;

import com.springboot.dgumarket.exception.CustomControllerExecption;
import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.repository.member.MemberRepository;
import com.springboot.dgumarket.service.UserDetailsImpl;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Optional;

@Component
@Aspect
public class CheckTargetUserValidateAOP {

    @Autowired
    MemberRepository memberRepository;

    @Pointcut("@annotation(CheckTargetUserValidate)")
    public void upLoadImagefile() {
    }


    @Before(value = "upLoadImagefile()")
    public void checkTargetUser(JoinPoint joinPoint) throws CustomControllerExecption {
        System.out.println("CheckTargetUserValidateAOP.checkTargetUser : aop 발동");
        Object[] parameterValues = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Optional<Member> targetMember = null;
        if(method.getParameters()[1].getName().equals("targetId")){ // targetId 가 1번에 들어오는 경우
            Integer targetUserId = (Integer)parameterValues[1];
            targetMember = memberRepository.findById(targetUserId);
            if (targetMember.isPresent()) {
                if (targetMember.get().getIsWithdrawn() == 1) {
                    throw new CustomControllerExecption("탈퇴한 유저에게 채팅 이미지를 전송할 수 없습니다.", HttpStatus.NOT_FOUND);
                }
                if (targetMember.get().getIsEnabled() == 1) {
                    throw new CustomControllerExecption("관리자로 부터 이용제재 받고 있는 유저에게 채팅 이미지를 전송할 수 없습니다.", HttpStatus.NOT_FOUND);
                }
            } else {
                throw new CustomControllerExecption("존재하지 않는 유저에게 채팅 이미지를 전송할 수 없습니다.", HttpStatus.NOT_FOUND);
            }
        }

        if(method.getParameters()[1].getName().equals("targetId") && method.getParameters()[0].getName().equals("authentication")){ // 차단관계 확인하기( 채팅인 경우에만 체크해야한다. 쓸데가 없음 )
            Authentication authentication = (Authentication) parameterValues[0];
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Member member = memberRepository.findById(userDetails.getId());
            if(targetMember.isPresent()){
                if (member.getBlockUsers().contains(targetMember.get())) { throw new CustomControllerExecption("차단한 유저와 채팅을 할 수 없습니다.", HttpStatus.BAD_REQUEST);}
                if (member.getUserBlockedMe().contains(targetMember.get())) {throw new CustomControllerExecption("차단당한 유저와 채팅을 할 수 없습니다.", HttpStatus.BAD_REQUEST);}
            }
        }
    }
}