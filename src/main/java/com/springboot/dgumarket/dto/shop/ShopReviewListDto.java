package com.springboot.dgumarket.dto.shop;

import com.springboot.dgumarket.dto.product.ProductReviewDto;
import lombok.*;

import java.util.List;

@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
public class ShopReviewListDto {
    private int total_size; // 전체 상품 개수(목록별, 전체|판매중|판매완료)
    private int page_size; // 페이지 별 가져오는 품목개수
    private List<ProductReviewDto> review_list;
}
