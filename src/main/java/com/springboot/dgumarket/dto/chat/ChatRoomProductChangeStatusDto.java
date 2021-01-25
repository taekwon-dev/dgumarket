package com.springboot.dgumarket.dto.chat;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ChatRoomProductChangeStatusDto {
    int transaction_status_id; // 상품거래상태
}
