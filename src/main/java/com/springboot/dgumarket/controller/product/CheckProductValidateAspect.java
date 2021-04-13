package com.springboot.dgumarket.controller.product;

import com.springboot.dgumarket.exception.notFoundException.ResultNotFoundException;
import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.model.product.Product;
import com.springboot.dgumarket.repository.member.MemberRepository;
import com.springboot.dgumarket.repository.product.ProductRepository;
import com.springboot.dgumarket.service.UserDetailsImpl;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Aspect
public class CheckProductValidateAspect {
    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ProductRepository productRepository;

    @Pointcut("@annotation(CheckProductValidate)")
    public void checkProductValidate() {}

    @Before(value = "checkProductValidate()")
    public void checkProductValidateAOP(JoinPoint joinPoint){
        System.out.println(joinPoint);

        int productId = (int)joinPoint.getArgs()[1];
        Optional<Product> product = productRepository.findById(productId);
        if(product.isPresent()){
            // 물건 삭제 / 유저 제재 되었을 경우 => 에러페이지 반환
            if (product.get().getProductStatus() == 1) throw new ResultNotFoundException("요청에 대한 결과를 조회할 수 없는 경우 -> 에러페이지 반환");
            if (product.get().getProductStatus() == 2) throw new ResultNotFoundException("요청에 대한 결과를 조회할 수 없는 경우 -> 에러페이지 반환");

            // 물건 판매자가 탈퇴/유저제재 되었을 경우 => 에러페이지 반환
            if (product.get().getMember().getIsWithdrawn() == 1) throw new ResultNotFoundException("요청에 대한 결과를 조회할 수 없는 경우 -> 에러페이지 반환");
            if (product.get().getMember().getIsEnabled() == 1) throw new ResultNotFoundException("요청에 대한 결과를 조회할 수 없는 경우 -> 에러페이지 반환");


            // 물건 판매자가 나와 차단관계일 경우 => 에러페이지 반환
            if(joinPoint.getArgs()[0] != null){
                Authentication authentication = (Authentication) joinPoint.getArgs()[0];
                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                Member member = memberRepository.findById(userDetails.getId());
                if(member.getBlockUsers().contains(product.get().getMember())) throw new ResultNotFoundException("요청에 대한 결과를 조회할 수 없는 경우 -> 에러페이지 반환");
                if(member.getUserBlockedMe().contains(product.get().getMember())) throw new ResultNotFoundException("요청에 대한 결과를 조회할 수 없는 경우 -> 에러페이지 반환");
            }
        }

    }


}
