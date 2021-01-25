package com.springboot.dgumarket.service.chat;

import com.springboot.dgumarket.model.chat.RedisChatRoom;

import java.util.Optional;

public interface RedisChatRoomService {
    Optional<RedisChatRoom> findByRoomId(int roomId);

    RedisChatRoom join(int roomId, int userId, String sessionId);

    RedisChatRoom leave(String roomId, String sesionId);

    void leave(String sessionId);
}
