package com.springboot.dgumarket.dto.product;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Created by TK YOUN (2020-12-22 오후 10:07)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */

@Getter
@Setter
public class ProductReadListDto {
    // 상품 고유 Id
    private int id;

    // 메인 이미지 저장 경로
    private String thumbnailImg;

    // 상품 타이틀
    private String title;

    // 상품 가격
    private String price;

    // 좋아요 수
    private int likeNums;

    // 개설된 채팅방 수
    private int chatroomNums;

    // 상품 카테고리 고유 아이디
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int category_id;

    // 내가 좋아요 했는지에 대한 유무
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private String likeStatus;

    // 상품 업로드 또는 마지막 수정 시간
    private LocalDateTime lastUpdatedDatetime;

    private LocalDateTime uploadDatetime;

    private int transaction_status_id;
}
