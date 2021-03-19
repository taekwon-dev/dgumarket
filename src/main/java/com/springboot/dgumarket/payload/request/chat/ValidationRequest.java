package com.springboot.dgumarket.payload.request.chat;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;


/**
 * 채팅방에서 물건페이지 또는 유저의 상점으로 이동시 먼저 유효성을 체크합니다
 * 유효성을 체크하여 정말 물건정보페이지 또는 프로필 페이지로 이동할 지는 나중에 결정합니다.
 */

@Getter
@Setter
public class ValidationRequest {
    private JsonNullable<Integer> product_id = JsonNullable.undefined(); // 채팅방에서 물건 클릭시
    private JsonNullable<Integer> user_id = JsonNullable.undefined(); // 상대방 프로필 클릭 시
}
