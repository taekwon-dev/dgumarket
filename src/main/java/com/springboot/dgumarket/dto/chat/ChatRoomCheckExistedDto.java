package com.springboot.dgumarket.dto.chat;

import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class ChatRoomCheckExistedDto {
    private boolean room_existed;
    private int room_id;
}
