package com.springboot.dgumarket.dto.chat;

import lombok.Getter;
import lombok.Setter;

 /**
 * 유저가 채팅방 목록에 들어갔을 떄 보여지는 채팅방 리스트 DTO
 */
@Getter
@Setter
public class ChatRoomListDto {
    int roomId; // 방번호
    ChatRoomUserDto chatRoomUserDto; // 최신메시지
    ChatRoomProductDto ChatRoomProductDto; // 물건
    ChatRoomRecentMessageDto chatRoomRecentMessageDto; // 최신메시지
    long unreadMessageCount; // 읽지않은메시지
}
