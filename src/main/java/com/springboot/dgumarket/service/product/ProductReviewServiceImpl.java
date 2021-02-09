package com.springboot.dgumarket.service.product;


import com.springboot.dgumarket.dto.product.ProductPurchaseDto;
import com.springboot.dgumarket.dto.product.ProductReviewDto;
import com.springboot.dgumarket.dto.shop.ShopReviewListDto;
import com.springboot.dgumarket.dto.shop.ShopPurchaseListDto;
import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.model.product.Product;
import com.springboot.dgumarket.model.product.ProductReview;
import com.springboot.dgumarket.payload.request.review.ProductCommentRequset;
import com.springboot.dgumarket.repository.member.MemberRepository;
import com.springboot.dgumarket.repository.product.ProductRepository;
import com.springboot.dgumarket.repository.product.ProductReviewRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    // 리뷰 조회하기
    @Override
    public ShopReviewListDto getReviews(int userId, Pageable pageable) {
        Member member = memberRepository.findById(userId);
        ModelMapper modelMapper = new ModelMapper();
        PropertyMap<ProductReview, ProductReviewDto> dtoPropertyMap = new PropertyMap<ProductReview, ProductReviewDto>() {
            @Override
            protected void configure() {
                map().setReview_user_id(source.getConsumer().getId());
                map().setReview_user_icon(source.getConsumer().getProfileImageDir());
                map().setReview_comment(source.getReviewMessage());
                map().setReview_date(source.getReviewRegistrationDate());
                map().setReview_nickname(source.getConsumer().getNickName());
            }
        };
        modelMapper.addMappings(dtoPropertyMap);
        List<ProductReviewDto> productReadListDtos;
        int totalNumber = productReviewRepository.countAllBySellerAndReviewMessageIsNotNull(member); // 총 메시지 개수
         productReadListDtos = productReviewRepository.findCompletedReviews(member, pageable)
                .stream()
                .map(productReview -> modelMapper.map(productReview, ProductReviewDto.class))
                .collect(Collectors.toList());

        return ShopReviewListDto.builder()
                .review_list(productReadListDtos)
                .page_size(productReadListDtos.size())
                .total_size(Math.toIntExact(totalNumber)).build();
    }


    // 유저가 구매한 물건 조회하기
    @Override
    public ShopPurchaseListDto getPurchaseProducts(int userId, String purchaseSet, Pageable pageable) {
        Member member = memberRepository.findById(userId);
        ModelMapper modelMapper = new ModelMapper();
        Converter<Object, Boolean> BooleanConverter = context -> (context.getSource() != null); // boolean 컨버터
        PropertyMap<ProductReview, ProductPurchaseDto> purchaseProductListPropertyMap = new PropertyMap<ProductReview, ProductPurchaseDto>() {
            @Override
            protected void configure() {
                map().setPurchase_seller_nickname(source.getSeller().getNickName());
                map().setPurchase_product_id(source.getId());
                map().setPurchase_title(source.getProduct().getTitle());
                map().setPurchase_price(source.getProduct().getPrice());
                map().setPurchase_date(source.getCreatedDate());
                map().setPurchase_like_num(source.getProduct().getLikeNums());
                map().setPurchase_product_img(source.getProduct().getImgDirectory());
                map().setPurchase_chat_num(source.getProduct().getChatroomNums());
                map().setPurchase_review_date(source.getReviewRegistrationDate());
                using(BooleanConverter).map(source.getReviewMessage()).set_review(false);
            }
        };
        modelMapper.addMappings(purchaseProductListPropertyMap);
        List<ProductPurchaseDto> productPurchaseDtos = new ArrayList<>();
        ShopPurchaseListDto shopPurchaseListDto = null;
        switch (purchaseSet){
            case "total" :
                productPurchaseDtos = productReviewRepository.findAllByConsumerId(userId, pageable)
                        .stream()
                        .map(productReview -> modelMapper.map(productReview, ProductPurchaseDto.class))
                        .collect(Collectors.toList());

                shopPurchaseListDto = ShopPurchaseListDto.builder()
                        .page_size(productPurchaseDtos.size())
                        .total_size(productReviewRepository.countAllByConsumer(member))
                        .purchase_product_list(productPurchaseDtos).build();
                break;
            case "write" :
                productPurchaseDtos = productReviewRepository.findCompletedReviews(member, pageable)
                        .stream()
                        .map(productReview -> modelMapper.map(productReview, ProductPurchaseDto.class))
                        .collect(Collectors.toList());

                shopPurchaseListDto = ShopPurchaseListDto.builder()
                        .page_size(productPurchaseDtos.size())
                        .total_size(productReviewRepository.countAllByConsumerAndReviewMessageIsNull(member))
                        .purchase_product_list(productPurchaseDtos).build();
                break;

            case "nowrite" :
                productPurchaseDtos = productReviewRepository.findUnCompletedReviews(member, pageable)
                        .stream()
                        .map(productReview -> modelMapper.map(productReview, ProductPurchaseDto.class))
                        .collect(Collectors.toList());

                shopPurchaseListDto = ShopPurchaseListDto.builder()
                        .page_size(productPurchaseDtos.size())
                        .total_size(productReviewRepository.countAllByConsumerAndReviewMessageIsNull(member))
                        .purchase_product_list(productPurchaseDtos).build();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + purchaseSet);
        }


        return shopPurchaseListDto;
    }
}
