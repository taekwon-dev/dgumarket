package com.springboot.dgumarket.controller.chat;

import com.springboot.dgumarket.exception.stomp.StompErrorException;
import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.payload.request.chat.SendMessage;
import com.springboot.dgumarket.repository.member.MemberRepository;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
@Aspect
public class MessageCheckAOP {

    @Autowired
    MemberRepository memberRepository;


    @Around("@annotation(MessageCheckValidate)") // 타겟은 해당 에노테이션이 있는 부분
    public void checkMessageAvailable(ProceedingJoinPoint joinPoint) throws StompErrorException {
        String parameterName;
        Object[] parameterValues = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        for (int i = 0; i < parameterValues.length; i++) {
            parameterName = method.getParameters()[i].getName();

            if (parameterName.equals("sendMessage")) { // 메시지일경우
                SendMessage sendMessage = (SendMessage) parameterValues[i];
                Member user = memberRepository.findById(sendMessage.getSenderId()); // 이미 여기서 탈퇴한 유저는 거른다.
                if(user == null) {
                    throw StompErrorException.builder().ERR_CODE(3).ERR_MESSAGE("탈퇴하거나, 존재하지 않는 유저는 메시지기능을 이용하실 수 없습니다.").build();
                }

                if(user.getIsEnabled()==1){
                    throw StompErrorException.builder().ERR_CODE(4).ERR_MESSAGE("관리자로부터 이용제재를 받고있습니다. 더 이상 서십스를 이용하실 수 없습니다.").build();
                }

                Member targetUser = memberRepository.findById(sendMessage.getReceiverId());

                if(targetUser==null){
                    throw StompErrorException.builder().ERR_CODE(5).ERR_MESSAGE("탈퇴하거나 존재하지 않는 유저에게 메시지를 보낼 수 없습니다.").build();
                }

                if(targetUser.getIsEnabled()==1){ // 관리자부터 제재를 받고 있는 유저에게 메시지 전송
                    throw StompErrorException.builder().ERR_CODE(6).ERR_MESSAGE("관리자로부터 제재를 받고있는 유저에게 메시지를 전달할 수 없습니다.").build();
                }

            }
        }
    }

}
