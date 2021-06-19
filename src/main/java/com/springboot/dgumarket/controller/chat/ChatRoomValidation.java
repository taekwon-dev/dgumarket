package com.springboot.dgumarket.controller.chat;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 사용처 : api/chatroom/find/room
// 개별물건정보페이지에서 "채팅으로거래하기"눌러서 채팅방이 존재하는 지 요청을 보낼 때
// 물건이 삭제되거나, 상대방 유저가 존재하지 않게되거나, 물건이 비공개처리되거나, 유저가 관리자에 의해 이용제재 조치 받았을 경우의 예외처리
@Target(value = ElementType.METHOD) // 어디에 쓸 지 정함
@Retention(RetentionPolicy.RUNTIME)  // 에노테이션 정보를 언제까지 유지할 것인가
public @interface ChatRoomValidation {
}
