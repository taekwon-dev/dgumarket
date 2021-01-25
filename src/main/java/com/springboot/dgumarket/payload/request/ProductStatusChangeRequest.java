package com.springboot.dgumarket.payload.request;

import lombok.Getter;

@Getter
public class ProductStatusChangeRequest {
    private int transaction_status_id; // 거래상태코드
}
