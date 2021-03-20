package com.springboot.dgumarket.repository.product;

import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.model.product.Product;
import com.springboot.dgumarket.model.product.ProductCategory;
import com.springboot.dgumarket.model.product.ProductReview;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * by ms , 2021-03-13 , customProductRepository
 */
@Repository
 public interface CustomProductRepository {
    PageImpl<Product> findAllPaging(@Nullable Member loginMember, Pageable pageable); // 전체물건리스트조회(optional, 로그인)
    PageImpl<Product> findAllPagingByCategory(@Nullable Member loginMember, int categoryId, Pageable pageable, @Nullable Product exceptProduct); // 카테고리리스트조회(optional, 로그인) , 해당카테고리의 다른 물건들 (제품상세페이지, 4개)
    PageImpl<Product> findUserProducts(@Nullable Member loginMember, Member user, @Nullable String productSet, Pageable pageable, @Nullable Product exceptProduct); // 유저판매물건조회(optional, 로그인), 또 다른 중고물품조회(제품상세페이지, 4개)
    PageImpl<ProductReview> findAllReviews(@Nullable Member loginMember, Member user, Pageable pageable); // 유저의 모든리뷰조회(optional, 로그인)
    PageImpl<Product> findAllFavoriteProducts(Member loginMember, Pageable pageable); // 관심물건조회(required, 로그인)
    PageImpl<ProductReview> findUserPurchases(Member loginMember, @Nullable String purchase_set, Pageable pageable); // 유저가 구매한 물건 조회하기(required, 로그인)


    List<Product> findIndexProductsByCategory(@Nullable Member loginMember, ProductCategory productCategory); // 인덱스, 카테고리별 및 관심 물건(optional, 로그인)
}
