package com.springboot.dgumarket.dto.chat;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * 유저가 채팅방 목록에 들어갔을 떄 보여지는 채팅방 리스트 가운데, 채팅상대방의 유저정보 가져올 떄 사용되는 DTO
 */
@Setter
@Getter
public class ChatRoomUserDto {

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    int user_id;

    String nickName;
}
