package com.springboot.dgumarket.dto.shop;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.springboot.dgumarket.dto.product.ProductReadListDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ShopFavoriteListDto {
    private int total_size; // 전체 상품 개수(목록별, 전체|판매중|판매완료)
    private int page_size; // 페이지 별 가져오는 품목개수
    private List<ProductReadListDto> productsList; // 물건 리스트
}
