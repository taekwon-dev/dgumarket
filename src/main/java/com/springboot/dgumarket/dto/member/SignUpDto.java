package com.springboot.dgumarket.dto.member;

import lombok.Getter;

import java.util.Set;

/**
 * Created by TK YOUN (2020-10-20 오전 8:38)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 *
 * 회원가입 DTO
 * Lombok @Setter - How to exclude some elements that I want.
 */

@Getter
public class SignUpDto {

    private String webMail;
    private String phoneNumber;
    private String nickName;
    private String password;
    Set<Integer> productCategories;
}
