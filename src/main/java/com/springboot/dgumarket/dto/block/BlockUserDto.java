package com.springboot.dgumarket.dto.block;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
public class BlockUserDto {
    private int id;
    private String nickName;
    private String profileImageDir;
    private int isBlock;
}
