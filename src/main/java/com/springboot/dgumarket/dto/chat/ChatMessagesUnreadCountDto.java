package com.springboot.dgumarket.dto.chat;


import lombok.Getter;
import lombok.Setter;

/**
 * 유저의 읽지 않은 메시지 총 개수를 반환함
 */
@Setter
@Getter
public class ChatMessagesUnreadCountDto {
    Integer unreadMessagesCnt;
}
