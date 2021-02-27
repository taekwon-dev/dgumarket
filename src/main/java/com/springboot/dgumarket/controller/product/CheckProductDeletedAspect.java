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
        if(product.get().getProductStatus() == 1){
            logger.info("[AOP] 물건삭제유무 에노테이션 - @CheckProductDeleted 실행");
            throw new CustomControllerExecption("존재하지 않은 상품입니다.", HttpStatus.NOT_FOUND);
        }
        return joinPoint.proceed();
    }
}
