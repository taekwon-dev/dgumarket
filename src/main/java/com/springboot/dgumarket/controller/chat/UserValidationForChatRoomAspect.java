package com.springboot.dgumarket.controller.chat;

import com.springboot.dgumarket.controller.shop.UserWithDrawnAspect;
import com.springboot.dgumarket.exception.CustomControllerExecption;
import com.springboot.dgumarket.model.chat.ChatRoom;
import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.model.product.Product;
import com.springboot.dgumarket.repository.chat.ChatRoomRepository;
import com.springboot.dgumarket.repository.member.MemberRepository;
import com.springboot.dgumarket.repository.product.ProductRepository;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Optional;


@Component
@Aspect
public class UserValidationForChatRoomAspect {

    Logger logger = LoggerFactory.getLogger(UserValidationForChatRoomAspect.class);

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ChatRoomRepository chatRoomRepository;

    @Autowired
    MemberRepository memberRepository;

    @Around("@annotation(UserValidationForChatRoom)") // 타겟은 해당 에노테이션이 있는 부분
    public Object checkUserValidation(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("(int) joinPoint.getArgs()[0] roomId : " + (int) joinPoint.getArgs()[0]);
        System.out.println("(int) joinPoint.getArgs()[1] userId : " + (int) joinPoint.getArgs()[1]);
        int roomId = (int) joinPoint.getArgs()[0];
        int userId = (int) joinPoint.getArgs()[1];

        Member member = memberRepository.findById(userId);
        Optional<ChatRoom> chatRoom = chatRoomRepository.findById(roomId);
        chatRoom.orElseThrow(()-> new CustomControllerExecption("존재하지 않는 채팅방입니다.", HttpStatus.NOT_FOUND));

        if(chatRoom.get().getMemberOpponent(member).getIsEnabled()==0){
            throw new CustomControllerExecption("관리자로 부터 이용제재 당하고있는 유저입니다.", HttpStatus.NOT_FOUND);
        }
        if(chatRoom.get().getMemberOpponent(member).getIsWithdrawn()==1){
            throw new CustomControllerExecption("탈퇴한 유저입니다.", HttpStatus.NOT_FOUND);
        }


        return joinPoint.proceed();
    }
}

