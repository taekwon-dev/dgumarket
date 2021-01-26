package com.springboot.dgumarket.service.product;


import com.springboot.dgumarket.dto.product.ProductReviewDto;
import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.model.product.Product;
import com.springboot.dgumarket.model.product.ProductReview;
import com.springboot.dgumarket.payload.request.review.ProductCommentRequset;
import com.springboot.dgumarket.repository.member.MemberRepository;
import com.springboot.dgumarket.repository.product.ProductRepository;
import com.springboot.dgumarket.repository.product.ProductReviewRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Created by MS KIM (2020-01-15)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */
@Slf4j
@Service
public class ProductReviewServiceImpl implements ProductReviewService{

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ProductReviewRepository productReviewRepository;


    // 거래후기 남기기
    @Override
    @Transactional
    public void addProductComment(int productId, int userId, ProductCommentRequset productCommentRequset) {

        Member member = memberRepository.getOne(userId);
        Product product = productRepository.getOne(productId);
        Optional<ProductReview> productReview = productReviewRepository.findByProduct(product);
        log.info("addProductComment 실행");
        if(productReview.isPresent()){
            if(productReview.get().getConsumer() == member){ // 정말 리뷰어 자격인지 확인
                LocalDateTime currentDate = LocalDateTime.now();

                productReview.get().setReviewMessage(productCommentRequset.getProduct_comment()); // 리뷰 추가
                productReview.get().setReviewRegistrationDate(currentDate.plusHours(9L));
            }
        }
    }

    // 거래후기 보기
    @Override
    public ProductReviewDto getProductComment(int productId, int userId) {

        Member member = memberRepository.getOne(userId);
        Product product = productRepository.getOne(productId);

        Optional<ProductReview> productReview = productReviewRepository.findByProduct(product);
        if(productReview.isPresent()){
            if(productReview.get().getSeller() == member || productReview.get().getConsumer() == member){ // 판매자 or 구매자 이라면
                return ProductReviewDto.builder()
                        .review_comment(productReview.get().getReviewMessage())
                        .review_nickname(productReview.get().getConsumer().getNickName())
                        .review_date(productReview.get().getReviewRegistrationDate().minusHours(9L)).build();
            }
        }
        return null;
    }
}
