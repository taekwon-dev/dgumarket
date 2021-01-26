package com.springboot.dgumarket.dto.chat;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ChatMessageUserDto {

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    int userId;
    String nickName;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    String profileImgPath;
}
