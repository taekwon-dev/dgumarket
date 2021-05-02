package com.springboot.dgumarket.controller.product;

import com.amazonaws.services.xray.model.Http;
import com.springboot.dgumarket.exception.CustomControllerExecption;
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
import org.springframework.http.HttpStatus;
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
    public void checkProductValidateAOP(JoinPoint joinPoint) throws CustomControllerExecption {
        System.out.println(joinPoint);

        int productId = (int)joinPoint.getArgs()[1];
        Optional<Product> product = productRepository.findById(productId);
        product.orElseThrow(() -> new CustomControllerExecption("삭제되거나 존재하지 않은 물건입니다.", HttpStatus.NOT_FOUND, "/shop/main/index"));
        if(product.isPresent()){
            // 물건 삭제 / 비공개처리 되었을 경우 => 에러페이지 반환
            if (product.get().getProductStatus() == 1) throw new CustomControllerExecption("삭제되거나 존재하지 않은 물건입니다.", HttpStatus.NOT_FOUND, "/shop/main/index");
            if (product.get().getProductStatus() == 2) throw new CustomControllerExecption("관리자에 의해 비공개 처리된 물건입니다.", HttpStatus.NOT_FOUND, "/shop/main/index");

            // 물건 판매자가 탈퇴/유저제재 되었을 경우 => 에러페이지 반환
            if (product.get().getMember().getIsWithdrawn() == 1) throw new CustomControllerExecption("물건의 판매자가 탈퇴하여 물건을 조회할 수 없습니다.", HttpStatus.NOT_FOUND, "/shop/main/index");
            if (product.get().getMember().getIsEnabled() == 1) throw new CustomControllerExecption("물건의 판매자가 관리자로 부터 이용제재조치를 받고 있어 물건을 조회할 수 없습니다.", HttpStatus.NOT_FOUND, "/shop/main/index");


            // 물건 판매자가 나와 차단관계일 경우 => 에러페이지 반환
            if(joinPoint.getArgs()[0] != null){
                Authentication authentication = (Authentication) joinPoint.getArgs()[0];
                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                Member member = memberRepository.findById(userDetails.getId());
                if(member.getBlockUsers().contains(product.get().getMember())) throw new CustomControllerExecption("차단한 유저의 물건을 조회할 수 없습니다.", HttpStatus.NOT_FOUND, "/shop/main/index");
                if(member.getUserBlockedMe().contains(product.get().getMember())) throw new CustomControllerExecption("차단당한 유저의 물건을 조회할 수 없습니다.", HttpStatus.NOT_FOUND, "/shop/main/index");
            }
        }
    }
}
