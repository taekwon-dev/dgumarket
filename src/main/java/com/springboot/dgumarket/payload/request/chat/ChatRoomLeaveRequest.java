package com.springboot.dgumarket.payload.request.chat;

import lombok.Getter;
import org.springframework.security.core.Authentication;

/**
 * created by ms, 2021-01-21
 *  서버로 채팅방 나가기 요청시 요청 BODY 에 room_leave : true 값을 포함하여 요청
 * {@link com.springboot.dgumarket.controller.chat.ChatRoomController#leaveChatRoom(int, ChatRoomLeaveRequest, Authentication)} 참고}
 */
@Getter
public class ChatRoomLeaveRequest {
   private boolean room_leave;
}
