package com.springboot.dgumarket.dto.chat;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 채팅방 목록 불러올 떄 개별 채팅방에 들어가는 최근 채팅메시지 형태
 */
@Getter
@Setter
public class ChatRoomRecentMessageDto {
    int message_type; // 메시지타입
    LocalDateTime message_date; // 메시지 보낸 날짜
    String message; // 메시지 내용
}
