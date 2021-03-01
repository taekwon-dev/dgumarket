package com.springboot.dgumarket.controller.shop;

import com.springboot.dgumarket.dto.block.BlockStatusDto;
import com.springboot.dgumarket.dto.member.MemberInfoDto;
import com.springboot.dgumarket.dto.shop.ShopFavoriteListDto;
import com.springboot.dgumarket.dto.shop.ShopReviewListDto;
import com.springboot.dgumarket.dto.shop.ShopPurchaseListDto;
import com.springboot.dgumarket.exception.CustomControllerExecption;
import com.springboot.dgumarket.payload.response.ApiResponseEntity;
import com.springboot.dgumarket.dto.shop.ShopProductListDto;
import com.springboot.dgumarket.service.UserDetailsImpl;
import com.springboot.dgumarket.service.block.UserBlockService;
import com.springboot.dgumarket.service.member.MemberProfileService;
import com.springboot.dgumarket.service.product.ProductReviewService;
import com.springboot.dgumarket.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class ShopController {
    private static final int DEFAULT_PAGE_SIZE = 20; // 기본사이즈

    private final UserBlockService userBlockService;
    private final MemberProfileService memberService;
    private final ProductService productService;
    private final ProductReviewService productReviewService;

    @GetMapping("/{userId}/shop-profile")
    @CheckUserIsWithDrawn
    public ResponseEntity<?> getUserProfiles(@PathVariable int userId, Authentication authentication) throws CustomControllerExecption {
        MemberInfoDto memberInfoDto = memberService.fetchMemberInfo(userId);
        if(authentication != null){ // 로그인 상태
            UserDetailsImpl userDetails = (UserDetailsImpl)authentication.getPrincipal();
            if(userDetails.getId() == userId){  // 자신의 프로필
                ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                        .message("my_profile")
                        .status(200)
                        .data(memberInfoDto).build();

                return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
            }else{ // 다른 유저의 프로필
                BlockStatusDto blockStatusDto = userBlockService.checkBlockStatus(userDetails.getId(), userId);
                if ( blockStatusDto.getBlock_status() == 3){ // 아무도 차단하지 않은 상태
                    ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                            .message("user_profile")
                            .status(200)
                            .data(memberInfoDto).build();
                    return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);

                }else {
                    throw new CustomControllerExecption("Unable to access blocked user.", HttpStatus.FORBIDDEN); // 접근불가
                }
            }
        }else{ // 비로그인 상태
            log.info("not login shopController");
            ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                    .message("user_profile")
                    .status(200)
                    .data(memberInfoDto).build();
            return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
        }
    }

    @GetMapping("/{userId}/products")
    @CheckUserIsWithDrawn
    public ResponseEntity<?> getUserProducts (
            @PathVariable("userId") int userId,
            Authentication authentication,
            @RequestParam(value = "product_set", defaultValue = "total", required = false) String productSet,
            @PageableDefault(size = DEFAULT_PAGE_SIZE)
            @SortDefault.SortDefaults({
                    @SortDefault(sort = "createDatetime", direction = Sort.Direction.DESC)
            }) Pageable pageable) throws CustomControllerExecption {
        if(authentication != null){
            log.info(pageable.toString());
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            if(userDetails.getId() == userId){
                ShopProductListDto shopProductListDto = productService.getUserProducts(userId, productSet, pageable);
                ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                        .message("my_products_sort_" + productSet)
                        .status(200)
                        .data(shopProductListDto).build();
                return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
            }else{
                BlockStatusDto blockStatusDto = userBlockService.checkBlockStatus(userDetails.getId(), userId);
                if(blockStatusDto.getBlock_status() == 3){ // 차단되지 않은 상태
                    ShopProductListDto shopProductListDto = productService.getUserProducts(userId, productSet, pageable);
                    ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                            .message("user_products_sort_" + productSet)
                            .status(200)
                            .data(shopProductListDto).build();
                    return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
                }else{
                    throw new CustomControllerExecption("Unable to access blocked user.", HttpStatus.FORBIDDEN);
                }
            }
        }else{
            // 비로그인상태
            ShopProductListDto shopProductListDto = productService.getUserProducts(userId, productSet, pageable);
            ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                    .message("user_products_sort_" + productSet)
                    .status(200)
                    .data(shopProductListDto).build();
            return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
        }
    }

    // 유저에게 남긴 리뷰 조회하기
    @GetMapping("/{userId}/reviews")
    @CheckUserIsWithDrawn
    public ResponseEntity<?> getUserReviews(
            @PathVariable int userId,
            Authentication authentication,
            @PageableDefault(size = DEFAULT_PAGE_SIZE)
            @SortDefault(sort = "ReviewRegistrationDate", direction = Sort.Direction.DESC) Pageable pageable) throws CustomControllerExecption {
        if(authentication != null){

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

                    if(userDetails.getId() != userId){//로그인
                        BlockStatusDto blockStatusDto = userBlockService.checkBlockStatus(userDetails.getId(), userId);
                        if (blockStatusDto.getBlock_status() == 3){ // 차단되지 않은 경우
                            ShopReviewListDto productReviewDtoList = productReviewService.getReviews(userId, pageable);
                    ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                            .message("review_messages")
                            .status(200)
                            .data(productReviewDtoList).build();
                    return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
                }else { throw new CustomControllerExecption("Unable to access blocked user.", HttpStatus.FORBIDDEN); } // 차단된 경우
            }else { // 나 자신이 조회하는 경우
                ShopReviewListDto productReviewDtoList = productReviewService.getReviews(userId, pageable);
                ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                        .message("my_review_messages")
                        .status(200)
                        .data(productReviewDtoList).build();
                return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
            }
        }else { //비로그인
            ShopReviewListDto productReviewDtoList = productReviewService.getReviews(userId, pageable);
            ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                    .message("review_messages")
                    .status(200)
                    .data(productReviewDtoList).build();
            return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
        }
    }

    // 유저의 구매물품 조회하기
    @GetMapping("/purchase/products")
    public ResponseEntity<?> getUserPurchase(
            Authentication authentication,
            @RequestParam(value = "purchase_set", defaultValue = "total", required = false) String purchase_set,
            @PageableDefault(size = DEFAULT_PAGE_SIZE)
            @SortDefault(sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) throws CustomControllerExecption{
        if (authentication != null){
            UserDetailsImpl userDetails = (UserDetailsImpl)authentication.getPrincipal();
            ShopPurchaseListDto shopPurchaseListDto = productReviewService.getPurchaseProducts(userDetails.getId(), purchase_set, pageable);
            ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                    .message("purchase products")
                    .status(200)
                    .data(shopPurchaseListDto).build();
            return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
        }
        throw new CustomControllerExecption("403 Forbidden, Wrong access", HttpStatus.FORBIDDEN);
    }

    // 유저 관심물건 조회하기
    @GetMapping("/favorites")
    public ResponseEntity<?> getUserFavorites(
            Authentication authentication,
            @PageableDefault(size = DEFAULT_PAGE_SIZE)
            @SortDefault(sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {

        if (authentication != null){
            log.info("로그인성공");
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            log.info("로그인성공  아이디 {}",userDetails.getId());
            ShopFavoriteListDto shopFavoriteListDto = productService.getFavoriteProducts(userDetails, pageable);
            ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                    .message("favorites products")
                    .status(200)
                    .data(shopFavoriteListDto).build();
            return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
        }

        return null;
    }
}
