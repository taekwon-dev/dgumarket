package com.springboot.dgumarket.service.product;

import com.springboot.dgumarket.dto.product.ProductCreateDto;
import com.springboot.dgumarket.dto.shop.ShopProductListDto;
import com.springboot.dgumarket.payload.response.ProductListIndex;
import com.springboot.dgumarket.service.UserDetailsImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Created by TK YOUN (2020-12-22 오후 10:08)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */
public interface ProductService {

    // 상품 등록
    String enrollProduct(ProductCreateDto productCreateDto);

    // 비로그인 상태 -> /api/product/index 요청, lastCategoryId -> 비로그인 상황에서 무한스크롤 감지 했을 때 인기카테고리가 계속 반환되는 문제 해결
    List<ProductListIndex>  indexNotLoggedIn(int lastCategoryId);

    // 로그인 상태 -> /api/product/index 요청, userDetails : '유저'의 관심 카테고리를 출력하기 위함, lastCategoryId -> 페이징 시 가장 마지막으로 응답했던 카테고리 id
    List<ProductListIndex> indexLoggedIn(UserDetailsImpl userDetails, int lastCategoryId);

    // 사용자 판매물품 조회 -> /api/shop/{userId}/products 요청
    ShopProductListDto getUserProducts(int userId, String productSet, Pageable pageable);

    // 카테고리별 상품 불러오기(비로그인)
    ShopProductListDto getCategoryProductsNotLoggedIn(int categoryId, Pageable pageable);

    // 카테고리별 상품 불러오기(로그인)
    ShopProductListDto getCategoryProductsLoggedIn(UserDetailsImpl userDetails, int categoryId, Pageable pageable);
}
