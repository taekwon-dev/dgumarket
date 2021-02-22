package com.springboot.dgumarket.payload.request.product;

import com.springboot.dgumarket.payload.request.report.ReportRequest;
import lombok.Getter;
import org.springframework.security.core.Authentication;

import javax.validation.constraints.NotBlank;


/**
 * created by ms, 2021-01-25
 *  판매자가 채팅방에서 판매중을 거래완료로 바꿀 때 서버로 보내는 요청 BODY 의 형태( transaction_status_id : 2 )
 * {@link com.springboot.dgumarket.controller.chat.ChatRoomController#updateProductStatus(ProductStatusChangeRequest, int)}  참고}
 */
@Getter
public class ProductStatusChangeRequest {
    private int transaction_status_id; // 거래상태코드
}
