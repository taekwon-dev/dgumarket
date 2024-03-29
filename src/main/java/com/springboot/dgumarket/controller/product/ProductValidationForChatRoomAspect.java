package com.springboot.dgumarket.controller.product;


import com.springboot.dgumarket.exception.CustomControllerExecption;
import com.springboot.dgumarket.model.product.Product;
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
public class ProductValidationForChatRoomAspect {

    Logger logger = LoggerFactory.getLogger(ProductValidationForChatRoomAspect.class);

    @Autowired
    ProductRepository productRepository;

    @Around("@annotation(ProductValidationForChatRoom)") // 타겟은 해당 에노테이션이 있는 부분
    public Object checkDeleted(ProceedingJoinPoint joinPoint) throws Throwable {

        logger.info("AOP발동, {}",joinPoint);

        Optional<Product> product = productRepository.findById((Integer) joinPoint.getArgs()[1]);

        // 상품정보 Status 값이 삭제된 경우 (식별자) | 삭제되었을 경우
        if(!product.isPresent() || product.get().getProductStatus() == 1){ // 유저 물건 자체 삭제
            throw new CustomControllerExecption("판매자에 의해 삭제처리된 중고물품 입니다.", HttpStatus.BAD_REQUEST, null, 100);
        }

        if(product.get().getMember().getIsWithdrawn()==1){// 탈퇴
            throw new CustomControllerExecption("탈퇴한 유저의 물건은 조회할 수 없습니다.", HttpStatus.BAD_REQUEST, null, 102);
        }

        if(product.get().getMember().getIsEnabled()==1){ // 이용제재
            throw new CustomControllerExecption("관리자에 의해 이용제재 받고 있는 유저의 물건은 조회할 수 없습니다.", HttpStatus.BAD_REQUEST, null, 103);
        }

        if(product.get().getProductStatus() == 2){ // 관리자에 의한 블라인드 처리
            throw new CustomControllerExecption("해당 중고물품은 관리자에 의해 비공개 처리되었습니다.", HttpStatus.BAD_REQUEST, null, 101);
        }

        return joinPoint.proceed();
    }
}
