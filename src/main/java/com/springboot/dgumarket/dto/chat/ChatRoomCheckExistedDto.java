package com.springboot.dgumarket.dto.chat;

import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
public class ChatRoomCheckExistedDto {
    private boolean room_existed;
    private int room_id;
}
