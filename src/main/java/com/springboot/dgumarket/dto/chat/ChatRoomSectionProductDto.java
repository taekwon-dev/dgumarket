package com.springboot.dgumarket.dto.chat;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@Builder
@ToString
public class ChatRoomSectionProductDto {

    private int product_id; // 물건 번호
    private String product_title; // 상품 타이틀
    private String product_price; // 상품가격
    private String product_img_path; // 상품이미지 경로
    private int transaction_status_id; // 거래상태

////    // 채팅방 들어갔을 떄 메시지들을 조회하는 경우에는 보여주지 않는다. ( 최초 채팅거래하기 눌러 채팅방 들어갈 떄는 보여주지 않는다.)
//    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
//    private boolean is_mine; // 내 물건인지 확인
}
