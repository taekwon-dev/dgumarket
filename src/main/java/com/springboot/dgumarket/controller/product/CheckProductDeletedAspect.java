package com.springboot.dgumarket.controller.product;

import com.springboot.dgumarket.controller.shop.UserWithDrawnAspect;
import com.springboot.dgumarket.exception.CustomControllerExecption;
import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.model.product.Product;
import com.springboot.dgumarket.repository.member.MemberRepository;
import com.springboot.dgumarket.repository.product.ProductRepository;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Aspect
public class CheckProductDeletedAspect {

    Logger logger = LoggerFactory.getLogger(UserWithDrawnAspect.class);

    @Autowired
    ProductRepository productRepository;

    @Around("@annotation(CheckProductDeleted)") // 타겟은 해당 에노테이션이 있는 부분
    public Object checkDeleted(ProceedingJoinPoint joinPoint) throws Throwable {
        Optional<Product> product = productRepository.findById((Integer) joinPoint.getArgs()[1]);
        product.orElseThrow(()->new CustomControllerExecption("존재하지 않은 물건입니다.", HttpStatus.NOT_FOUND));
        if(product.get().getMember().getIsWithdrawn()==1){// 탈퇴
            throw new CustomControllerExecption("탈퇴한 유저의 물건은 조회할 수 없습니다.", HttpStatus.BAD_REQUEST);
        }
        if(product.get().getMember().getIsEnabled()==1){ // 이용제재
            throw new CustomControllerExecption("관리자에 의해 이용제재 초지받고 있는 유저의 물건은 조회할 수 없습니다.", HttpStatus.BAD_REQUEST);
        }
        if(product.get().getProductStatus() == 1){ // 유저 물건 자체 삭제
            logger.info("[AOP] 물건삭제유무 에노테이션 - @CheckProductDeleted 실행");
            throw new CustomControllerExecption("존재하지 않은 상품입니다.", HttpStatus.NOT_FOUND);
        }
        if(product.get().getProductStatus() == 2){ // 관리자에 의한 블라인드 처리
            throw new CustomControllerExecption("관리자에 의해 블라인드 처리된 상태입니다.", HttpStatus.NOT_FOUND);
        }
        return joinPoint.proceed();
    }
}
