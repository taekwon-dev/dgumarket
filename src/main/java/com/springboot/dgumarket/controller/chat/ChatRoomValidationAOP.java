package com.springboot.dgumarket.controller.chat;


import com.springboot.dgumarket.exception.CustomControllerExecption;
import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.model.product.Product;
import com.springboot.dgumarket.repository.member.MemberRepository;
import com.springboot.dgumarket.repository.product.ProductRepository;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * {@link com.springboot.dgumarket.controller.chat.ChatRoomController}
 * findChatRoom 에 대한 AOP 이다.
 * "채팅으로거래하기" 요청하기 전 유저가 이미 해당 물건에 대해서 채팅을 한 이력이 있어서 채팅방이 존재하는 지 존재하지 않은 지에 대한 유무를 검사한다.
 * 다만 예외의 상황이 발생할 경우를 대비하여 controller 로 가기전에 미리 검사한다. 예외의 상황과 예외를 검사하는 순서는 아래와 같다.
 *
 * 1. 유저가 탈퇴했을 경우
 * 2. 유저가 관리자에 의해 유저제재 조치 받았을 경우
 * 3. 물건이 삭제되었을 경우
 * 4. 물건이 관리자에 의해 비공개 처리 되었을 경우
 * 5. 해당 물건을 올린 유저와 차단관계에 있을 경우( 내가 물건을 올린 상대방을 차단한 경우 / 물건을 올린 상대방으로부터 내가 차단된 경우 )
 *
 */

@Component
@Aspect
public class ChatRoomValidationAOP {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MemberRepository memberRepository;


    @Pointcut("@annotation(ChatRoomValidation)")
    public void chatRoomValidate() {
    }

    // 해당 어노테이션이 있는 곳 호출되기 before(이전)에 먼저 아래의 aop 가 발동되도록 함
    @Before(value = "chatRoomValidate()")
    public void chatRoomValidateCheck(JoinPoint joinPoint) throws CustomControllerExecption {

        // 1. 유저가 탈퇴했을 경우
        int sellerId = (int) joinPoint.getArgs()[1];
        Member opponentMember = memberRepository.findById(sellerId);
        if(opponentMember==null || opponentMember.getIsWithdrawn() ==1){

        }

        // 2. 유저가 관리자에 의해 이용제재
        if(opponentMember.getIsEnabled()==1){

        }

        // 3. 물건이 삭제되거나 존재하지 않은 물건에 대해서 요청했을 때
        int productId = (int) joinPoint.getArgs()[0];
        Product product = productRepository.findByIdNotOptional(productId);
        if(product.getProductStatus()==1 || product == null){

        }

        // 4. 물건이 관리자에 의해 비공개 처리 되었을 경우
        if(product.getProductStatus()==2){

        }


        int myId = (int)joinPoint.getArgs()[2];
        Member loginMember = memberRepository.findById(myId);
        // 5-1. 내가 물건을 올린 상대방을 차단했을 경우
        if(loginMember.getBlockUsers().contains(opponentMember)){

        }

        // 5-2. 물건을 올린 상대방으로부터 내가 차단된 경우
        if(loginMember.getUserBlockedMe().contains(opponentMember)){

        }
    }
}
