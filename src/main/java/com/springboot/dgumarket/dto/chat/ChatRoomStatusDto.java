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

    private boolean isWarn; // 경고유무

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int block_status; // 차단상태 (1: 내가 상대방 차단(상대방이 나를 차단한 경우도 포함), 2.내가 상대방을 차단하지 않았지만 상대방이 나를 차단한 경우, 3. 서로 아무도 차단하지 않은 상태)
}
