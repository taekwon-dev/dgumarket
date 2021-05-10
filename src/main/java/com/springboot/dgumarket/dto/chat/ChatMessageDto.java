package com.springboot.dgumarket.dto.chat;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 실시간으로 수신받는 채팅 메시지 형태 DTO
 */
@Setter
@Getter
@ToString
public class ChatMessageDto {
    int roomId; // 메시지가 속한 방번호
    int message_type; // 메시지타입
    int messageStatus; // 메시지읽음여부
    String message; // 메시지내용
    LocalDateTime messageDate; // 메시지작성시간
    ChatMessageUserDto chatMessageUserDto; // 유저정보

    // 채팅방 들어갔을 떄 메시지들을 조회하는 경우에는 보여주지 않는다.
    @JsonInclude(JsonInclude.Include.NON_NULL)
    ChatRoomProductDto chatRoomProductDto; // 물건정보
}
