package com.springboot.dgumarket.dto.chat;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class ChatRoomTradeHistoryDto {
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    int history_room_id;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    int history_product_id;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    boolean isExisted;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    boolean isLeave;
}
