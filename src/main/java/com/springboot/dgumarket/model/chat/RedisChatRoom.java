package com.springboot.dgumarket.model.chat;

import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisHash;



import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@Getter
@Data
@ToString
@RedisHash("chatroom")
public class RedisChatRoom implements Serializable {
    private static Logger logger = LoggerFactory.getLogger(RedisChatRoom.class);

    @Id
    private String roomId;

    private List<RedisChatUser> connectedUsers = new ArrayList<>();

    public RedisChatRoom(){

    }

    @Builder
    public RedisChatRoom(String roomId){
        this.roomId = roomId;
    }



    // [채팅 방에 들어있는 유저를 가져온다]
    public List<RedisChatUser> getConnectedUsers() {
        return this.connectedUsers;
    }

    // [채팅방 인원 추가] 레디스 채팅방에 유저 추가
    public void addUser(RedisChatUser user) {
        this.connectedUsers.add(user);
    }

    // [채팅방 인원 삭제] 채팅방 나갈시, 레디스 채팅방에 나간 유저를 찾아 해당 유저를 제거
    public void removeUser(String sessionId) {
        this.connectedUsers.removeIf(e -> e.getSessionId().equals(sessionId));
    }



    // [채팅방 인원 수]
    public int getNumberOfConnectedUsers(){
        return this.connectedUsers.size();
    }

    /**
     * @param receiverId 나의 아이디(보내는 사람 번호)
     * @return 현재 채팅방에 다른 사람이 있는 지 없는 지 유뮤
     */
    public boolean isSomeoneInChatRoom(String receiverId){
        logger.info("[REDIS, chatroom] get connectedUsers : {}", this.getConnectedUsers());
        logger.info("[REDIS, chatroom] is {} in chatroom ?", receiverId);
        for (RedisChatUser u : getConnectedUsers()) {
            logger.info("[REDIS, chatroom] user : {}", u.toString());
        }
        return this.getConnectedUsers().stream().anyMatch(e -> e.getUserId() == Integer.parseInt(receiverId));
    }
}
