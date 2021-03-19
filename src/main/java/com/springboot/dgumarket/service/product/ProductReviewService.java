package com.springboot.dgumarket.service.product;

import com.springboot.dgumarket.dto.product.ProductReviewDto;
import com.springboot.dgumarket.dto.shop.ShopReviewListDto;
import com.springboot.dgumarket.dto.shop.ShopPurchaseListDto;
import com.springboot.dgumarket.exception.CustomControllerExecption;
import com.springboot.dgumarket.payload.request.review.ProductCommentRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;

import javax.validation.constraints.Null;


/**
 * Created by MS KIM (2020-01-15)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 *
 * addProductComment -> 거래 후기 남기기(구매자)
 * getProductComment -> 거래 후기 보기(구매자, 판매자)
 *
 * -----
 * 02-02
 * ShopReviewListDto  getReviews(int userId, Pageable pageable); // 유저에게 남긴 물품리뷰 조회
 * ShopPurchaseListDto getPurchaseProducts(int userId, Pageable pageable); // 유저의 구매물품 조회
 */

public interface ProductReviewService {
    void addProductComment(int productId, int userId, ProductCommentRequest commentRequset) throws CustomControllerExecption;
    ProductReviewDto getProductComment(int productId, int userId) throws CustomControllerExecption;
    ShopReviewListDto getReviews(@Nullable Integer loginUser, @Nullable Integer userId , Pageable pageable) throws CustomControllerExecption; // 유저에게 남긴 물품리뷰 조회(optional 로그인)
    ShopPurchaseListDto getPurchaseProducts(int loginUser, String purchaseSet, Pageable pageable); // 유저의 구매물품 조회(required 로그인)
}
