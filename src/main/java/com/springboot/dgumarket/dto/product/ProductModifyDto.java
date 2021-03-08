package com.springboot.dgumarket.dto.product;

import lombok.Getter;

/**
 * Created by TK YOUN (2021-03-06 오전 11:50)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */
@Getter
public class ProductModifyDto {
    private int productId;                                                 // 상품 고유 아이디
    private String title;                                                  // 상품 타이틀 (글 제목)
    private String information;                                            // 상품 정보
    private String price;                                                  // 상품 가격 (0원 입력 시 - 무료나눔)
    private String imgDirectory;                                           // 저장된 상품 이미지 디렉토리 (최대 5장)
    private int productCategory;                                           // 상품 카테고리 코드
    private int isNego;                                                    // 가격 조율 가능 여부 (0 : 가능, 1 : 불가능)
    private int transactionStatusId;                                       // 거래 상태 코드
    private int transactionModeId;                                         // 거래 방식 코드
    private int selfProductStatus;                                         // [삭제예정]
}
