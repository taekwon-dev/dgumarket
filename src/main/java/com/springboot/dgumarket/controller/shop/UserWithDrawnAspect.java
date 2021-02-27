package com.springboot.dgumarket.controller.shop;

import com.springboot.dgumarket.exception.CustomControllerExecption;
import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.repository.member.MemberRepository;
import com.springboot.dgumarket.service.UserDetailsImpl;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Aspect
public class UserWithDrawnAspect {

    Logger logger = LoggerFactory.getLogger(UserWithDrawnAspect.class);

    @Autowired
    MemberRepository memberRepository;

    @Around("@annotation(CheckUserIsWithDrawn)") // 타겟은 해당 에노테이션이 있는 부분
    public Object checkWithDrawn(ProceedingJoinPoint joinPoint) throws Throwable {
        Optional<Member> member = memberRepository.findById((Integer) joinPoint.getArgs()[0]);
        if(member.get().getIsWithdrawn() == 1){
            logger.info("[AOP] 유저탈퇴유무 에노테이션 - @CheckUserIsWithDrawn 실행, 탈퇴유저 조회시도!!");
            throw new CustomControllerExecption("존재하지 않은 유저 입니다", HttpStatus.NOT_FOUND);
        }
        return joinPoint.proceed();
    }


}
