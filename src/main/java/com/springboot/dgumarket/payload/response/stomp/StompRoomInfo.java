package com.springboot.dgumarket.payload.response.stomp;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class StompRoomInfo {
    private String roomId;

    public StompRoomInfo(String roomId){
        this.roomId = roomId;
    }
}
