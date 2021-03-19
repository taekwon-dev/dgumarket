package com.springboot.dgumarket.dto.chat;


import lombok.Getter;
import lombok.Setter;

/**
 * 유저가 채팅방 목록에 들어갔을 떄 보여지는 채팅방 리스트 가운데, 물건정보를 담는 DTO
 */
@Getter
@Setter
public class ChatRoomProductDto {
    int product_id; // 추가됨
    int product_deleted;
    String productImgPath;
}
