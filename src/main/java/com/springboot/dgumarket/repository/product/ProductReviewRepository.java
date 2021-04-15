package com.springboot.dgumarket.repository.product;

import com.springboot.dgumarket.model.chat.ChatRoom;
import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.model.product.Product;
import com.springboot.dgumarket.model.product.ProductReview;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductReviewRepository extends JpaRepository<ProductReview, Integer> {

    Optional<ProductReview> findByChatRoom(ChatRoom chatRoom);
    Optional<ProductReview> findByProduct(Product product);

//    // 작성 리뷰 조회(판매자에게 남긴 글), /api/shop/{userId}/reviews
//    @Query("select r from ProductReview r where r.seller = :user and r.reviewMessage is not null")
//    List<ProductReview> findReviews(@Param("user") Member member, Pageable pageable);
//
//    // 모든 구매물품 보기(리뷰X, 리뷰O) , /api/shop/{userId}/purchase?purchase_set=total
//    List<ProductReview> findAllByConsumerId(int userId, Pageable pageable);
//
//    // 구매후기 작성한 구매물품 조회, /api/shop/{userId}/purchase?purchase_set=write
//    @Query("select r from ProductReview r where r.consumer = :user and r.reviewMessage is not null")
//    List<ProductReview> findWritedReviewsPurchase (@Param("user") Member member, Pageable pageable);
//
//    // 아직 구매후기 작성하지안은 구매물품 조회 , /api/shop/{userId}/purchase?purchase_set=nowrite
//    @Query("select r from ProductReview r where r.consumer = :user and r.reviewMessage is null")
//    List<ProductReview> findNoWritedReviewsPurchase (@Param("user") Member member, Pageable pageable);
//
//    // 유저에게 남겨진 거래리뷰 개수 조회(거래리뷰 남긴 것들)
//    int countAllBySellerAndReviewMessageIsNotNull(Member member);
//
//    // 아직 작성하지 않은 구매물품의 개수(=미작성인 리뷰의 개수) , /api/shop/{userId}/purchase?purchase_set=nowrite
//    int countAllByConsumerAndReviewMessageIsNull(Member member);
//
//    // 리뷰 작성까지 완료한 구매물품의 개수(=작성완료인 리뷰의 개수), /api/shop/{userId}/purchase?purchase_set=write
//    int countAllByConsumerAndReviewMessageIsNotNull(Member member);
//
//    // 내가 구매한 물건 개수 조회하기
//    int countAllByConsumer(Member member);

    // 상대방과 거래한 적 있는 지 조회하기
    @Query("select r from ProductReview r " +
            "where (r.seller = :user and r.consumer =:target) or " +
            "(r.consumer =:user and r.seller =:target)")
    List<ProductReview> checkTradeHistory(@Param("user") Member user, @Param("target") Member target);

}
