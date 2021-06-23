package com.springboot.dgumarket.service.product;


import com.springboot.dgumarket.dto.product.ProductPurchaseDto;
import com.springboot.dgumarket.dto.product.ProductReviewDto;
import com.springboot.dgumarket.dto.shop.ShopReviewListDto;
import com.springboot.dgumarket.dto.shop.ShopPurchaseListDto;
import com.springboot.dgumarket.exception.CustomControllerExecption;
import com.springboot.dgumarket.model.member.BlockUser;
import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.model.product.Product;
import com.springboot.dgumarket.model.product.ProductReview;
import com.springboot.dgumarket.payload.request.review.ProductCommentRequest;
import com.springboot.dgumarket.repository.member.BlockUserRepository;
import com.springboot.dgumarket.repository.member.MemberRepository;
import com.springboot.dgumarket.repository.product.CustomProductRepository;
import com.springboot.dgumarket.repository.product.ProductRepository;
import com.springboot.dgumarket.repository.product.ProductReviewRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
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
    private MemberRepository memberRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductReviewRepository productReviewRepository;

    @Autowired
    private CustomProductRepository customProductRepository;

    @Autowired
    private BlockUserRepository blockUserRepository;

    // 거래후기 남기기
    @Override
    @Transactional
    public void addProductComment(int productId, int userId, ProductCommentRequest productCommentRequest) throws CustomControllerExecption {

        // 로그인 유저
        Member member = memberRepository.getOne(userId);

        // 상품 객체 조회 (via ProdictId)
        Product product = productRepository.getOne(productId);

        // 해당 상품 업로더
        Member productUploader = product.getMember();


        // 상대방이 유저제재/탈퇴/차단관계 일경우 예외처리하기
        if (productUploader == null || productUploader.getIsWithdrawn() == 1) throw new CustomControllerExecption("탈퇴한 유저에게 거래후기를 남길 수 없습니다.", HttpStatus.BAD_REQUEST, null, 102);
        if (productUploader.getIsEnabled() == 1) throw new CustomControllerExecption("이용제재를 받고 있는 유저에게 거래후기를 작성할 수 없습니다.", HttpStatus.BAD_REQUEST, null, 103);


        // 차단 여부 조회 (양방향)
        // loginUser.getBlockUsers() : where user_id = loginUser.id
        BlockUser blockUser = blockUserRepository.findByUserAndBlockedUser(member, productUploader);

        // loginUser.getUserBlockedMe() : where blocked_user_id = loginUser.id
        BlockUser blockedUser = blockUserRepository.findByUserAndBlockedUser(productUploader, member);

        if (member.getBlockUsers().contains(blockUser)) {
            throw new CustomControllerExecption("차단한 유저에게 거래후기를 작성할 수 없습니다.", HttpStatus.BAD_REQUEST, null, 104);
        }

        if (member.getUserBlockedMe().contains(blockedUser)) {
            throw new CustomControllerExecption("나를 차단한 유저에게 거래후기를 작성할 수 없습니다.", HttpStatus.BAD_REQUEST, null, 105);
        }

        // 상대방 물건이 블라인드 또는 삭제 되었을 경우 예외처리하기
        if (product == null || product.getProductStatus() == 1) throw new CustomControllerExecption("해당 중고물품은 삭제처리되었습니다.", HttpStatus.BAD_REQUEST, null, 100);
        if (product.getProductStatus() == 2) throw new CustomControllerExecption("해당 중고물품은 관리자에 의해 비공개 처리되어 거래후기를 작성할 수 없습니다.", HttpStatus.BAD_REQUEST, null, 101);


        Optional<ProductReview> productReview = productReviewRepository.findByProduct(product);

        if (productReview.isPresent()) {
            if (productReview.get().getReviewMessage() != null) { // 이미 메시지가 작성되어 있는 상태라면
                throw new CustomControllerExecption("이미 거래후기를 작성하였습니다.", HttpStatus.BAD_REQUEST, null, 107);
            }

            if (productReview.get().getConsumer() == member) { // 정말 리뷰어 자격인지 확인
                LocalDateTime currentDate = LocalDateTime.now();
                productReview.get().setReviewMessage(productCommentRequest.getProduct_comment()); // 리뷰 추가
                productReview.get().setReviewRegistrationDate(currentDate);
            }
        }
    }

    // 거래후기 보기
    @Override
    public ProductReviewDto getProductComment(int productId, int userId) throws CustomControllerExecption {

        // 로그인 유저
        Member member = memberRepository.getOne(userId);

        // 상품 객체 조회 (via ProductId)
        Product product = productRepository.findById(productId);
        if (product == null || product.getProductStatus() == 1)
            throw new CustomControllerExecption("해당 중고물품은 삭제처리되었습니다.", HttpStatus.BAD_REQUEST, null, 100);

        if (product.getProductStatus() == 2)
            throw new CustomControllerExecption("해당 중고물품은 관리자에 의해 비공개 처리되었습니다.", HttpStatus.BAD_REQUEST, null, 101);



        Optional<ProductReview> productReview = productReviewRepository.findByProduct(product);
        if (productReview.isPresent()) {
            if (productReview.get().getSeller() == member || productReview.get().getConsumer() == member) { // 판매자 or 구매자 이라면


                // 해당 상품에 대해 로그인 유저 = 구매자인 경우
                if (productReview.get().getConsumer() == member){
                    if (productReview.get().getSeller() == null || productReview.get().getSeller().getIsWithdrawn() == 1) throw new CustomControllerExecption("탈퇴한 유저의 거래후기는 볼 수 없습니다.", HttpStatus.BAD_REQUEST, null, 102);
                    if (productReview.get().getSeller().getIsEnabled()==1) throw new CustomControllerExecption("이용제재를 받고 있는 유저의 거래후기는 조회할 수 없습니다.", HttpStatus.BAD_REQUEST, null, 103);

                    // 해당 상품의 판매자 (= 업로더)
                    Member productUploader = productReview.get().getSeller();

                    // 차단 여부 조회 (양방향)
                    // loginUser.getBlockUsers() : where user_id = loginUser.id
                    BlockUser blockUser = blockUserRepository.findByUserAndBlockedUser(member, productUploader);

                    // loginUser.getUserBlockedMe() : where blocked_user_id = loginUser.id
                    BlockUser blockedUser = blockUserRepository.findByUserAndBlockedUser(productUploader, member);

                    if (member.getBlockUsers().contains(blockUser)) { // 내가 차단한 상대라면
                        throw new CustomControllerExecption("차단한 유저에게 작성한 거래후기는 조회할 수 없습니다.", HttpStatus.BAD_REQUEST, null, 104);
                    }

                    if (member.getUserBlockedMe().contains(blockedUser)) {
                        throw new CustomControllerExecption("나를 차단한 유저에게 작성한 거래후기를 조회할 수 없습니다.", HttpStatus.BAD_REQUEST, null, 105);
                    }

                } else {
                    // 해당 상품에 대해 로그인 유저 = 판매자인 경우

                    if (productReview.get().getConsumer() == null || productReview.get().getConsumer().getIsWithdrawn() == 1) throw new CustomControllerExecption("탈퇴한 유저의 거래후기는 볼 수 없습니다.", HttpStatus.BAD_REQUEST, null, 102);
                    if (productReview.get().getConsumer().getIsEnabled() == 1) throw new CustomControllerExecption("이용제재를 받고 있는 유저의 거래후기는 조회할 수 없습니다.", HttpStatus.BAD_REQUEST, null, 103);

                    // 해당 상품의 구매자
                    Member productConsumer = productReview.get().getConsumer();

                    // 차단 여부 조회 (양방향)
                    // loginUser.getBlockUsers() : where user_id = loginUser.id
                    BlockUser blockUser = blockUserRepository.findByUserAndBlockedUser(member, productConsumer);

                    // loginUser.getUserBlockedMe() : where blocked_user_id = loginUser.id
                    BlockUser blockedUser = blockUserRepository.findByUserAndBlockedUser(productConsumer, member);


                    if (member.getBlockUsers().contains(blockUser)) {
                        throw new CustomControllerExecption("차단한 유저에 대한 거래후기를 조회할 수 없습니다.", HttpStatus.BAD_REQUEST, null, 104);
                    }

                    if (member.getUserBlockedMe().contains(blockedUser)) {
                        throw new CustomControllerExecption("나를 차단한 유저에 대한 거래후기를 조회할 수 없습니다.", HttpStatus.BAD_REQUEST, null, 105);
                    }
                }


                return ProductReviewDto.builder()
                        .review_user_id(productReview.get().getConsumer().getId())
                        .review_comment(productReview.get().getReviewMessage())
                        .review_nickname(productReview.get().getConsumer().getNickName())
                        .review_date(productReview.get().getReviewRegistrationDate().minusHours(9L)).build();
            }
        }
        return null;
    }

    // 리뷰 조회하기
    // 제재, 탈퇴, 차단 제외 조회
    @Override
    public ShopReviewListDto getReviews(Integer loginUserId, Integer userId, Pageable pageable) throws CustomControllerExecption {

        Optional<Member> user = memberRepository.findById(userId); // 조회유저아이디
//        user.orElseThrow(() -> new CustomControllerExecption("존재하지 않는 유저입니다.", HttpStatus.NOT_FOUND, null));

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
        PageImpl<ProductReview> productReviews = null;
        if(loginUserId == null){
            productReviews = customProductRepository.findAllReviews(null, user.get(), pageable);
        }else{
            Optional<Member> loginMember = memberRepository.findById(loginUserId);
//            loginMember.orElseThrow(() -> new CustomControllerExecption("존재하지 않는 유저입니다.", HttpStatus.NOT_FOUND, null));
            productReviews = customProductRepository.findAllReviews(loginMember.get(), user.get(), pageable);
        }

        modelMapper.addMappings(dtoPropertyMap);
        List<ProductReviewDto> productReadListDtos = productReviews.getContent().stream()
                .map(productReview -> modelMapper.map(productReview, ProductReviewDto.class))
                .collect(Collectors.toList());

        return ShopReviewListDto.builder()
                .review_list(productReadListDtos)
                .page_size(productReadListDtos.size())
                .total_size((int) productReviews.getTotalElements()).build();
    }


    // 유저가 구매한 물건 조회하기
    @Override
    public ShopPurchaseListDto getPurchaseProducts(int userId, String purchaseSet, Pageable pageable) {
        log.info("purchaseSet : {}", purchaseSet);
        Member member = memberRepository.findById(userId);
        ModelMapper modelMapper = new ModelMapper();
        Converter<Object, Boolean> BooleanConverter = context -> (context.getSource() != null); // boolean 컨버터

        PropertyMap<ProductReview, ProductPurchaseDto> purchaseProductListPropertyMap = new PropertyMap<ProductReview, ProductPurchaseDto>() {
            @Override
            protected void configure() {
                map().setPurchase_seller_nickname(source.getSeller().getNickName());
                map().setPurchase_product_id(source.getProduct().getId());
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

        PageImpl<ProductReview> products =  customProductRepository.findUserPurchases(member, purchaseSet, pageable);
        List<ProductPurchaseDto> productPurchaseDtos = products.getContent().stream()
                .map(productReview -> modelMapper.map(productReview, ProductPurchaseDto.class))
                .collect(Collectors.toList());

        return ShopPurchaseListDto.builder()
                .total_size((int)products.getTotalElements())
                .page_size(productPurchaseDtos.size())
                .purchase_product_list(productPurchaseDtos)
                .build();
    }
}
