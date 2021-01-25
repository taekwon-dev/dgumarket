package com.springboot.dgumarket.payload.response;

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
