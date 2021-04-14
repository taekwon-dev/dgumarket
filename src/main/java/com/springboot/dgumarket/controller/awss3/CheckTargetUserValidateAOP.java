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
        Member targetMember = null;
        // 채팅업로드에 대해서만 검사한다.
        if(joinPoint.getArgs()[2].equals("origin/chat/")){ // 이미지 업로드시에만 작동
            System.out.println("채팅이미지 업로드시 발동되는 aop");
            int targetId = (int)joinPoint.getArgs()[1];
            targetMember = memberRepository.findById(targetId);
            if(targetMember == null) throw new CustomControllerExecption("존재하지 않는 유저에게 채팅 이미지를 전송할 수 없습니다.", HttpStatus.NOT_FOUND, null);
            if(targetMember.getIsWithdrawn() == 1) throw new CustomControllerExecption("탈퇴한 유저에게 채팅 이미지를 전송할 수 없습니다.", HttpStatus.NOT_FOUND, null);
            if(targetMember.getIsEnabled() == 1) throw new CustomControllerExecption("관리자로부터 이용제재 받고 있는 유저에게 채팅 이미지를 전송할 수 없습니다.", HttpStatus.NOT_FOUND, null);

            // 차단유무에 대해서 검사
            Authentication authentication = (Authentication)joinPoint.getArgs()[0];
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Member member = memberRepository.findById(userDetails.getId());
            if (member.getBlockUsers().contains(targetMember))  throw new CustomControllerExecption("차단한 유저와 채팅을 할 수 없습니다.", HttpStatus.BAD_REQUEST, null);
            if (member.getUserBlockedMe().contains(targetMember)) throw new CustomControllerExecption("차단당한 유저와 채팅을 할 수 없습니다.", HttpStatus.BAD_REQUEST, null);
        }
    }
}