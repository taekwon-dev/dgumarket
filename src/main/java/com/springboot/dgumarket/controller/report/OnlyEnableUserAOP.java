package com.springboot.dgumarket.controller.report;

import com.springboot.dgumarket.controller.shop.UserWithDrawnAspect;
import com.springboot.dgumarket.exception.CustomControllerExecption;
import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.model.product.Product;
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
public class OnlyEnableUserAOP {
    Logger logger = LoggerFactory.getLogger(OnlyEnableUserAOP.class);

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ProductRepository productRepository;

    @Around("@annotation(OnlyEnableUser)") // 타겟은 해당 에노테이션이 있는 부분
    public Object doCheckEnableUser(ProceedingJoinPoint joinPoint) throws Throwable {
        String parameterName;
        Object[] parameterValues = joinPoint.getArgs();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        for (int i = 0; i < parameterValues.length; i++) {
            parameterName = method.getParameters()[i].getName();

            if(parameterName.equals("userId")){ // 유저아이디로 접근하는 부분 ( 유저 샵 정보를 보여주기위한 API 들 )
                int userId = (int) parameterValues[i];
                Member member = memberRepository.findById(userId);
                    if(member.getIsEnabled() == 1){ // 관리자로부터 이용재제 상태
                        throw new CustomControllerExecption("이용재제당한 유저로의 접근 불가", HttpStatus.BAD_REQUEST);
                }
            }else if(parameterName.equals("productId")){ // 재제당한 유저의 물건정보를 요구하는 경우

                int productId = (int) parameterValues[i];
                Optional<Product> product = productRepository.findById(productId);
                if(product.isPresent()){
                    if (product.get().getMember().getIsEnabled() == 1){
                        throw new CustomControllerExecption("이용재제당한 유저의 물건 접근불가", HttpStatus.BAD_REQUEST);
                    }
                }

            }

        }





        return joinPoint.proceed();
    }

}



