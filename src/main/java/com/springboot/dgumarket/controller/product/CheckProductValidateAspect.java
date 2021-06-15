package com.springboot.dgumarket.controller.product;


import com.springboot.dgumarket.exception.CustomControllerExecption;
import com.springboot.dgumarket.model.member.BlockUser;
import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.model.product.Product;
import com.springboot.dgumarket.repository.member.BlockUserRepository;
import com.springboot.dgumarket.repository.member.MemberRepository;
import com.springboot.dgumarket.repository.product.ProductRepository;
import com.springboot.dgumarket.service.UserDetailsImpl;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Aspect
public class CheckProductValidateAspect {
    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BlockUserRepository blockUserRepository;

    @Pointcut("@annotation(CheckProductValidate)")
    public void checkProductValidate() {}

    @Before(value = "checkProductValidate()")
    public void checkProductValidateAOP(JoinPoint joinPoint) throws CustomControllerExecption {

        int productId = (int)joinPoint.getArgs()[1];
        Product product = productRepository.findById(productId);

        if (product == null) throw new CustomControllerExecption("삭제되거나 존재하지 않은 물건입니다.", HttpStatus.NOT_FOUND, "/shop/main/index");

        // 물건 삭제 / 비공개처리 되었을 경우 => 에러페이지 반환
        if (product.getProductStatus() == 1) throw new CustomControllerExecption("삭제되거나 존재하지 않은 물건입니다.", HttpStatus.NOT_FOUND, "/shop/main/index");
        if (product.getProductStatus() == 2) throw new CustomControllerExecption("관리자에 의해 비공개 처리된 물건입니다.", HttpStatus.NOT_FOUND, "/shop/main/index");

        // 물건 판매자가 탈퇴/유저제재 되었을 경우 => 에러페이지 반환
        if (product.getMember().getIsWithdrawn() == 1) throw new CustomControllerExecption("물건의 판매자가 탈퇴하여 물건을 조회할 수 없습니다.", HttpStatus.NOT_FOUND, "/shop/main/index");
        if (product.getMember().getIsEnabled() == 1) throw new CustomControllerExecption("물건의 판매자가 관리자로 부터 이용제재조치를 받고 있어 물건을 조회할 수 없습니다.", HttpStatus.NOT_FOUND, "/shop/main/index");


        // 물건 판매자가 나와 차단관계일 경우 => 에러페이지 반환
        if(joinPoint.getArgs()[0] != null){
            Authentication authentication = (Authentication) joinPoint.getArgs()[0];
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            // 로그인 유저
            Member loginUser = memberRepository.findById(userDetails.getId());

            // 상품 업로드한 유저
            // 상품 -> 멤버 객체탐색 (상품 정보를 통해 해당 업로더의 고유 ID 조회)
            Member productUploader = memberRepository.findById(product.getMember().getId());

            // loginUser.getBlockUsers() : where user_id = loginUser.id
            // (= 로그인한 유저가 차단한 유저 리스트)
            BlockUser blockUser = blockUserRepository.findByUserAndBlockedUser(loginUser, productUploader);

            // loginUser.getUserBlockedMe() : where blocked_user_id = loginUser.id
            // (= 로그인한 유저를 차단한 리스트), 따라서 타겟 유저가 로그인 유저를 차단한 경우를 포함한다.
            BlockUser blockedUser = blockUserRepository.findByUserAndBlockedUser(productUploader, loginUser);

            if (loginUser.getBlockUsers().contains(blockUser))
                throw new CustomControllerExecption("차단한 유저의 물건을 조회할 수 없습니다.", HttpStatus.NOT_FOUND, "/shop/main/index");
            if (loginUser.getUserBlockedMe().contains(blockedUser))
                throw new CustomControllerExecption("나를 차단한 유저의 물건을 조회할 수 없습니다.", HttpStatus.NOT_FOUND, "/shop/main/index");
        }

    }
}
