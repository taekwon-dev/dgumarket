package com.springboot.dgumarket.controller.awss3;

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
public class CheckTargetUserValidateAOP {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BlockUserRepository blockUserRepository;

    @Pointcut("@annotation(CheckTargetUserValidate)")
    public void upLoadImagefile() {
    }


    @Before(value = "upLoadImagefile()")
    public void checkTargetUser(JoinPoint joinPoint) throws CustomControllerExecption {

        Member targetMember = null;

        // 채팅 이미지 업로드할 때만 작동하는 AOP
        if(joinPoint.getArgs()[2].equals("origin/chat/")){

            int targetId = (int) joinPoint.getArgs()[1];

            // 채팅 상대 유저
            targetMember = memberRepository.findById(targetId);

            if (targetMember == null) throw new CustomControllerExecption("존재하지 않는 유저에게 채팅 이미지를 전송할 수 없습니다.", HttpStatus.BAD_REQUEST, null, 102);
            if (targetMember.getIsWithdrawn() == 1) throw new CustomControllerExecption("탈퇴한 유저에게 채팅 이미지를 전송할 수 없습니다.", HttpStatus.BAD_REQUEST, null, 102);
            if (targetMember.getIsEnabled() == 1) throw new CustomControllerExecption("관리자로부터 이용제재 받고 있는 유저에게 채팅 이미지를 전송할 수 없습니다.", HttpStatus.BAD_REQUEST, null, 103);

            // 차단유무에 대해서 검사
            Authentication authentication = (Authentication)joinPoint.getArgs()[0];
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            // 로그인 유저
            Member loginUser = memberRepository.findById(userDetails.getId());

            // loginUser.getBlockUsers() : where user_id = loginUser.id
            // (= 로그인한 유저가 차단한 유저 리스트)
            BlockUser blockUser = blockUserRepository.findByUserAndBlockedUser(loginUser, targetMember);

            // loginUser.getUserBlockedMe() : where blocked_user_id = loginUser.id
            // (= 로그인한 유저를 차단한 리스트), 따라서 타겟 유저가 로그인 유저를 차단한 경우를 포함한다.
            BlockUser blockedUser = blockUserRepository.findByUserAndBlockedUser(targetMember, loginUser);


            if (loginUser.getBlockUsers().contains(blockUser))  throw new CustomControllerExecption("차단한 유저와는 채팅을 할 수 없습니다.", HttpStatus.BAD_REQUEST, null, 104);
            if (loginUser.getUserBlockedMe().contains(blockedUser)) throw new CustomControllerExecption("나를 차단한 유저와는 채팅을 할 수 없습니다.", HttpStatus.BAD_REQUEST, null, 105);
        }
    }
}