package com.springboot.dgumarket.controller.shop;

import com.springboot.dgumarket.exception.CustomControllerExecption;
import com.springboot.dgumarket.exception.notFoundException.PreMemberNotFoundException;
import com.springboot.dgumarket.exception.notFoundException.ResultNotFoundException;
import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.repository.member.MemberRepository;
import com.springboot.dgumarket.service.UserDetailsImpl;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Optional;

@Component
@Aspect
public class ShopValidateImpl {

    Logger logger = LoggerFactory.getLogger(ShopValidate.class);

    @Autowired
    MemberRepository memberRepository;

    @Pointcut("@annotation(ShopValidate)")
    public void shopValidate() {
    }

    @Before(value = "shopValidate()")
    public void shopValidateCheck(JoinPoint joinPoint) throws CustomControllerExecption{
        logger.info("shop controller aop, shopValidate");
        int userId = (int)joinPoint.getArgs()[0]; // target user id
        Member targetMember = memberRepository.findById(userId);
        if(targetMember == null || targetMember.getIsWithdrawn() == 1) throw new CustomControllerExecption("존재하지 않거나 탈퇴한 유저 입니다.", HttpStatus.NOT_FOUND, "/shop/main/index");
        if(targetMember.getIsEnabled() == 1) throw new CustomControllerExecption("관리자로부터 이용제재 받고 있는 유저입니다.", HttpStatus.NOT_FOUND, "/shop/main/index");


        // 상대방이 나와 차단관계에 있을 경우 -> 에러페이지 반환
        if(joinPoint.getArgs()[1] != null){ // login my user id
            Authentication authentication = (Authentication)joinPoint.getArgs()[1];
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Member member = memberRepository.findById(userDetails.getId());
            if(member.getBlockUsers().contains(targetMember)) throw new CustomControllerExecption("차단한 유저에 대한 정보를 조회할 수 없습니다.", HttpStatus.NOT_FOUND, "/shop/main/index");
            if(member.getUserBlockedMe().contains(targetMember)) throw new CustomControllerExecption("차단당한 유저의 정보를 조회할 수 없습니다.", HttpStatus.NOT_FOUND, "/shop/main/index");
        }
    }


}
