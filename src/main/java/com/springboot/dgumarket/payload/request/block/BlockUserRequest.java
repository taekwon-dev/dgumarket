package com.springboot.dgumarket.payload.request.block;

import lombok.Getter;
import org.springframework.security.core.Authentication;

/**
  * created by ms, 2021-01-23
 *  사용자 차단시 요청 BODY 에 차단할 사용자 ID 를 포함하여 요청
  * {@link com.springboot.dgumarket.controller.member.MemberBlockController#blockUser(Authentication, BlockUserRequest)} 참고}
 */
@Getter
public class BlockUserRequest {
    private int block_user;
}
