package com.springboot.dgumarket.dto.chat;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ChatMessageUserDto {
    int userId;
    String nickName;
    String profileImgPath;
}
