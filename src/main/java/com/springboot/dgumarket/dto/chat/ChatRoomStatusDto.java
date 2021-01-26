package com.springboot.dgumarket.dto.chat;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ChatRoomStatusDto {
    /**
     * 0: 판매자가 구매완료 누르지 않은 상태에서 구매자가 채팅방에 들어갈 경우
     * 1: 판매자가 구매완료 눌렀을 상태에서 구매자가 채팅방에 들어갈 경우
     * 2: 판매자가 채팅방에 들어갈 경우
     * 3: 해당채팅방에 있는 물건이 삭제되었을 경우!
     */
    private int productStatus;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int transactionStatus; // 거래완료 유무 (1: 완료)

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int isReviewUpload; // 후기 썻는 지 유무 (1: 후기 씀)

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String message; // 메시지 내용

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String reviewer_nickname; // 리뷰어 닉네임
}
