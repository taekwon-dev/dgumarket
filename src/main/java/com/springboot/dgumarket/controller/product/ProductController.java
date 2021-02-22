package com.springboot.dgumarket.controller.product;

import com.springboot.dgumarket.dto.product.ProductCreateDto;
import com.springboot.dgumarket.dto.product.ProductReadOneDto;
import com.springboot.dgumarket.exception.CustomControllerExecption;
import com.springboot.dgumarket.payload.request.PagingIndexRequest;
import com.springboot.dgumarket.payload.request.product.LikeRequest;
import com.springboot.dgumarket.payload.response.ApiResponseEntity;
import com.springboot.dgumarket.payload.response.ProductListIndex;
import com.springboot.dgumarket.service.UserDetailsImpl;
import com.springboot.dgumarket.service.product.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;

/**
 * Created by TK YOUN (2020-12-22 오후 10:08)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */
@Slf4j
@RestController
@RequestMapping("/api/product")
public class ProductController {

    private ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/create")
    public ResponseEntity<String> doEnrollProduct(@RequestBody ProductCreateDto productCreateDto) {
        String product = productService.enrollProduct(productCreateDto);
        return new ResponseEntity<>(product, HttpStatus.CREATED);
    }

    @PostMapping("/index")
    public ResponseEntity<List<ProductListIndex>> findIndexTest(Authentication authentication, @RequestBody PagingIndexRequest lastCategoryId) {

        if (authentication != null) {
            // 로그인 상태 (-> '유저의 관심' 카테고리)
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            List<ProductListIndex> productCategorySet = productService.indexLoggedIn(userDetails, lastCategoryId.getLastCategoryId());
            return new ResponseEntity<>(productCategorySet, HttpStatus.OK);
        } else {
            // 비로그인 상태 (-> '인기' 카테고리)
            List<ProductListIndex>  products = productService.indexNotLoggedIn(lastCategoryId.getLastCategoryId());
            return new ResponseEntity<>(products, HttpStatus.OK);
        }
    }

    // 물건 조회하기
    @GetMapping("/{productId}/info")
    public ResponseEntity<?> getProductInfo(
            Authentication authentication,
            @PathVariable("productId") int productId) throws CustomControllerExecption {

        if(authentication != null){
            UserDetailsImpl userDetails = (UserDetailsImpl)authentication.getPrincipal();
            ProductReadOneDto readOneDto = productService.getProductInfo(userDetails, productId);
            ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                    .message("물건 조회")
                    .status(200)
                    .data(readOneDto).build();
            return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
        }
        ProductReadOneDto readOneDto = productService.getProductInfo(null, productId);
        ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                .message("물건 조회")
                .status(200)
                .data(readOneDto).build();
        return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
    }

    // 좋아요 기능
    @PatchMapping("/product-like")
    public ResponseEntity<?> cancelLikeProduct(
            Authentication authentication,
            @Valid @RequestBody LikeRequest likeRequest, Errors errors) throws CustomControllerExecption {

        if(authentication != null){
            // 유효성 에러
            if (errors.hasErrors()) { // 400
                throw new CustomControllerExecption(Objects.requireNonNull(errors.getFieldError()).getDefaultMessage(), HttpStatus.BAD_REQUEST);
            }
            String resultMessage;
            UserDetailsImpl userDetails = (UserDetailsImpl)authentication.getPrincipal();
            String currentStatus = productService.changeLikeProduct(userDetails, likeRequest);
            if (currentStatus.equals("like")){
                resultMessage = "좋아요 요청";
            }else{
                resultMessage = "좋아요 취소 요청";
            }
            ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                    .status(200)
                    .message(resultMessage)
                    .data("current_status_" + currentStatus).build();
            return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
        }

        return null;
    }

}
