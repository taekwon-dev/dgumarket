package com.springboot.dgumarket.service.chat;

import com.springboot.dgumarket.dto.chat.ChatMessageDto;
import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.payload.request.chat.SendMessage;

import java.util.List;

/**
 * 21/01/04 18:33, created by ms
 */
public interface ChatMessageService {
    // 읽지 않은 메시지 가져오기
    Integer findUnreadMessages(int userId);
    List<ChatMessageDto> getAllMessages(int roomId, Member member); // 채팅방 나갔을 경우 메시지 가져오기
    ChatMessageDto save(SendMessage message, String sessionId); // 메시지 저장 for STOMP
}
