package com.springboot.dgumarket.controller.shop;

import com.springboot.dgumarket.exception.CustomControllerExecption;
import com.springboot.dgumarket.model.member.BlockUser;
import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.repository.member.BlockUserRepository;
import com.springboot.dgumarket.repository.member.MemberRepository;
import com.springboot.dgumarket.service.UserDetailsImpl;
import org.aspectj.lang.JoinPoint;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import org.springframework.security.core.Authentication;

import org.springframework.stereotype.Component;


@Component
@Aspect
public class ShopValidateImpl {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BlockUserRepository blockUserRepository;

    @Pointcut("@annotation(ShopValidate)")
    public void shopValidate() {
    }

    @Before(value = "shopValidate()")
    public void shopValidateCheck(JoinPoint joinPoint) throws CustomControllerExecption{

        // 타겟 유저의 고유 ID
        int userId = (int) joinPoint.getArgs()[0];

        // 타겟 유저 (=조회하려는 유저)
        Member targetMember = memberRepository.findById(userId);


        if (targetMember == null || targetMember.getIsWithdrawn() == 1) throw new CustomControllerExecption("존재하지 않거나 탈퇴한 유저 입니다.", HttpStatus.NOT_FOUND, "/shop/main/index");
        if (targetMember.getIsEnabled() == 1) throw new CustomControllerExecption("관리자로부터 이용제재 받고 있는 유저입니다.", HttpStatus.NOT_FOUND, "/shop/main/index");


        // 상대방이 나와 차단관계에 있을 경우 -> 에러페이지 반환
        if(joinPoint.getArgs()[1] != null){ // login my user id
            Authentication authentication = (Authentication)joinPoint.getArgs()[1];
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            // 로그인 유저
            Member loginUser = memberRepository.findById(userDetails.getId());

            // loginUser.getBlockUsers() : where user_id = loginUser.id
            // (= 로그인한 유저가 차단한 유저 리스트)
            BlockUser blockUser = blockUserRepository.findByUserAndBlockedUser(loginUser, targetMember);

            // loginUser.getUserBlockedMe() : where blocked_user_id = loginUser.id
            // (= 로그인한 유저를 차단한 리스트), 따라서 타겟 유저가 로그인 유저를 차단한 경우를 포함한다.
            BlockUser blockedUser = blockUserRepository.findByUserAndBlockedUser(targetMember, loginUser);

            if (loginUser.getBlockUsers().contains(blockUser))
                throw new CustomControllerExecption("차단한 유저에 대한 정보를 조회할 수 없습니다.", HttpStatus.NOT_FOUND, "/shop/main/index");

            if (loginUser.getUserBlockedMe().contains(blockedUser))
                throw new CustomControllerExecption("나를 차단한 유저의 정보를 조회할 수 없습니다.", HttpStatus.NOT_FOUND, "/shop/main/index");
        }
    }


}
