package com.springboot.dgumarket.controller.product;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 탈퇴유무를 검사하는 커스텀 에노테이션
@Target(value = ElementType.METHOD) // 어디에 쓸 지 정함
@Retention(RetentionPolicy.RUNTIME)  // 에노테이션 정보를 언제까지 유지할 것인가
public @interface CheckProductDeleted {
}
