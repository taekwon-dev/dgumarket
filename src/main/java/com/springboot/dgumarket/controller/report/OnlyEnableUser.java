package com.springboot.dgumarket.controller.report;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 이용자 재제 유무 체크
@Target(value = ElementType.METHOD) // 어디에 쓸 지 정함
@Retention(RetentionPolicy.RUNTIME)  // 에노테이션 정보를 언제까지 유지할 것인가
public @interface OnlyEnableUser {
}
