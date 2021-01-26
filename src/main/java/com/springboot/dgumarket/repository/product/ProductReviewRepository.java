package com.springboot.dgumarket.repository.product;

import com.springboot.dgumarket.model.chat.ChatRoom;
import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.model.product.Product;
import com.springboot.dgumarket.model.product.ProductReview;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductReviewRepository extends JpaRepository<ProductReview, Integer> {

    Optional<ProductReview> findByChatRoom(ChatRoom chatRoom);
    Optional<ProductReview> findByProduct(Product product);

    // 1.16 TEST, 유저의 구매물품 가져오기
    List<ProductReview> findAllByConsumerId(int userId);

    // 1.16 TEST, 유저의 구매후기 조회하기
    List<ProductReview> findAllBySellerAndReviewMessageIsNotNull(Member member);

    @Query("select r from ProductReview r where r.seller = :user and r.reviewMessage is not null")
    List<ProductReview> findCompletedReviews(@Param("user") Member member);

    // 상대방과 거래한 적있는 지 조회하기
    @Query("select r from ProductReview r " +
            "where (r.seller = :user and r.consumer =:target) or " +
            "(r.consumer =:user and r.seller =:target)")
    List<ProductReview> checkTradeHistory(@Param("user") Member user, @Param("target") Member target);

}
