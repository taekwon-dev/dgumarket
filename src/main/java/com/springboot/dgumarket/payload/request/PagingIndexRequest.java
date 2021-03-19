package com.springboot.dgumarket.payload.request;

import lombok.Getter;

/**
 * Created by TK YOUN (2020-12-30 오후 10:38)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description : 메인 (index.html) 페이지에서 '무한 스크롤링' 페이징 요청 시점에 가장 마지막으로 불러온 카테고리 고유 ID 값을 파라미터로 요청
 */
@Getter
public class PagingIndexRequest {
    int lastCategoryId;
}
