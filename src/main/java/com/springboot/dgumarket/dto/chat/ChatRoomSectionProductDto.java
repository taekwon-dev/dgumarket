package com.springboot.dgumarket.dto.chat;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@Builder
@ToString
public class ChatRoomSectionProductDto {

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int product_id; // 물건 번호

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String product_title; // 상품 타이틀

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String product_price; // 상품가격

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String product_img_path; // 상품이미지 경로

    private int transaction_status_id; // 거래상태
}
