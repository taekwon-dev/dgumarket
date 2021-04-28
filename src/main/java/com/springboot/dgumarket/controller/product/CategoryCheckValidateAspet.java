package com.springboot.dgumarket.controller.product;


import com.springboot.dgumarket.exception.CustomControllerExecption;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@Aspect
public class CategoryCheckValidateAspet {

    @Pointcut("@annotation(CategoryCheck)")
    public void checkProductCategoryValidate() {}

    @Before(value = "checkProductCategoryValidate()")
    public void checkProductValidateAOP(JoinPoint joinPoint) throws CustomControllerExecption {
        Object[] args = joinPoint.getArgs();
        int categoryNum = (int)args[1];
        if(!Arrays.asList(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15).contains(categoryNum)){
            throw new CustomControllerExecption("잘못된 경로입니다.", HttpStatus.NOT_FOUND, "/exceptions");
        };
    }

}
