package com.springboot.dgumarket.service.product;

import com.springboot.dgumarket.dto.product.ProductCreateDto;
import com.springboot.dgumarket.dto.product.ProductDeleteDto;
import com.springboot.dgumarket.dto.product.ProductModifyDto;
import com.springboot.dgumarket.dto.product.ProductReadOneDto;
import com.springboot.dgumarket.dto.shop.ShopFavoriteListDto;
import com.springboot.dgumarket.dto.shop.ShopProductListDto;
import com.springboot.dgumarket.exception.CustomControllerExecption;
import com.springboot.dgumarket.model.product.Product;
import com.springboot.dgumarket.payload.request.product.LikeRequest;
import com.springboot.dgumarket.payload.response.ProductListIndex;
import com.springboot.dgumarket.service.UserDetailsImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Created by TK YOUN (2020-12-22 오후 10:08)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */
public interface ProductService {

    // 상품 등록
    Product doUplaodProduct(ProductCreateDto productCreateDto);

    // 상품 정보 수정
    Product doUpdateProduct(ProductModifyDto productModifyDto);

    void doDeleteProduct(int productId);

    // 비로그인 상태 -> /api/product/index 요청, lastCategoryId -> 비로그인 상황에서 무한스크롤 감지 했을 때 인기카테고리가 계속 반환되는 문제 해결
    List<ProductListIndex>  indexNotLoggedIn(int lastCategoryId);

    // 로그인 상태 -> /api/product/index 요청, userDetails : '유저'의 관심 카테고리를 출력하기 위함, lastCategoryId -> 페이징 시 가장 마지막으로 응답했던 카테고리 id
    List<ProductListIndex> indexLoggedIn(UserDetailsImpl userDetails, int lastCategoryId);

    // 사용자 판매물품 조회 -> /user/{userId}/products 요청
    ShopProductListDto getUserProducts(
            @Nullable UserDetailsImpl userDetails,
            @Nullable Integer userId,
            String productSet,
            Pageable pageable,
            @Nullable Integer exceptPid
    );


    // 사용자 관심물건 조회 -> /user/favorites
    ShopFavoriteListDto getFavoriteProducts(UserDetailsImpl userDetails, Pageable pageable);

    // 카테고리별 상품 불러오기
    ShopProductListDto getProductsByCategory(@Nullable UserDetailsImpl userDetails, int categoryId, Pageable pageable, @Nullable Integer exceptPid);

    // 물건 전체보기
    ShopProductListDto getAllProducts(@Nullable UserDetailsImpl userDetails, Pageable pageable);

    // 물건 개별 정보 보기
    ProductReadOneDto getProductInfo(@Nullable UserDetailsImpl userDetails, int productId) throws CustomControllerExecption;

    // 물건 좋아요 & 취소하기
    String changeLikeProduct(UserDetailsImpl userDetails, LikeRequest likeRequest) throws CustomControllerExecption;

}
