package com.springboot.dgumarket.payload.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.springboot.dgumarket.dto.chat.ChatMessageUserDto;
import com.springboot.dgumarket.dto.chat.ChatRoomProductDto;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class StompReceivedMessage {
    int roomId; // 방 아이디
    int messageType; // 메시지타입
    int messageStatus; // 메시지읽음여부
    String message; // 메시지내용
    LocalDateTime messageDate; // 메시지작성시간
    ChatMessageUserDto chatMessageUserDto; // 유저정보

    // 채팅방 들어갔을 떄 메시지들을 조회하는 경우에는 보여주지 않는다.
    @JsonInclude(JsonInclude.Include.NON_NULL)
    ChatRoomProductDto chatRoomProductDto; // 물건정보
}
