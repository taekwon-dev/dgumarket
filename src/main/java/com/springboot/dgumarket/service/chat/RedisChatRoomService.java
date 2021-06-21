package com.springboot.dgumarket.service.chat;

import com.springboot.dgumarket.model.chat.RedisChatRoom;

import java.util.Optional;

public interface RedisChatRoomService {

    void join(int roomId, int userId, String sessionId);

    void leave(String roomId, String sessionId);

    void leave(String sessionId);
}
