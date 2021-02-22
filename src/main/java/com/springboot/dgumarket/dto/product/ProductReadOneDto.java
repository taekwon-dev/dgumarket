package com.springboot.dgumarket.dto.product;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
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
public class ProductReadOneDto {
    // 이미지 저장 경로
    private String ImgDirectories;

    // 상품 타이틀
    private String title;

    // 상품 정보
    private String information;

    // 상품 가격
    private String price;

    // 카테고리 코드
    private int categoryId;

    // 상품 카테고리명
    private String productCategory;

    // 거래 상태 코드
    private int transactionStatusId;

    // 거래 방식 코드
    private int transactionModeId;

    // 가격 조정 여부
    private int isNego;

    // 좋아요 수
    private int likeNums;

    // 조회 수
    private int viewNums;

    // 개설된 채팅방 수
    private int chatroomNums;

    // 판매자 자체 상품 등급 평가
    private int selfProductStatus;

    // 유저 고유 아이디 ( 클릭 이동 )
    private int userId;

    // 유저 프로필 사진
    private String profileImgDirectory;

    // 유저 닉네임
    private String userNickName;

    // 마지막 수정 시간
    private LocalDateTime lastUpdatedDatetime;

    // 업로드 시간
    private LocalDateTime uploadDatetime;

    // 물건 좋아요 여부
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String isLiked = "nolike";
}
