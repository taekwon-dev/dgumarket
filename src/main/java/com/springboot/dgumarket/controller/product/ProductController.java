package com.springboot.dgumarket.controller.product;

import com.springboot.dgumarket.dto.product.ProductCreateDto;
import com.springboot.dgumarket.dto.product.ProductReadOneDto;
import com.springboot.dgumarket.dto.shop.ShopProductListDto;
import com.springboot.dgumarket.exception.CustomControllerExecption;
import com.springboot.dgumarket.payload.request.PagingIndexRequest;
import com.springboot.dgumarket.payload.request.product.LikeRequest;
import com.springboot.dgumarket.payload.response.ApiResponseEntity;
import com.springboot.dgumarket.payload.response.ProductListIndex;
import com.springboot.dgumarket.service.UserDetailsImpl;
import com.springboot.dgumarket.service.product.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Null;
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

    private static final int DEFAULT_PAGE_SIZE = 20;
    private ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/create")
    public ResponseEntity<String> doEnrollProduct(@RequestBody ProductCreateDto productCreateDto) {
        String product = productService.enrollProduct(productCreateDto);
        return new ResponseEntity<>(product, HttpStatus.CREATED);
    }

    // 개별 물건 조회하기
    @GetMapping("/{productId}/info")
    @CheckProductDeleted
    public ResponseEntity<?> getProductInfo(
            Authentication authentication,
            @PathVariable("productId") int productId) throws CustomControllerExecption {

        if(authentication != null){
            UserDetailsImpl userDetails = (UserDetailsImpl)authentication.getPrincipal();
            ProductReadOneDto readOneDto = productService.getProductInfo(userDetails, productId);
            if(readOneDto.getUserId() == userDetails.getId()){
                log.info("readOneDto.getUserId() " + readOneDto.getUserId());
                log.info("userDetails.getId() " + userDetails.getId());
                ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                        .message("my_product")
                        .status(200)
                        .data(readOneDto).build();
                return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
            }
            ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                    .message("user_product")
                    .status(200)
                    .data(readOneDto).build();
            return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
        }

        ProductReadOneDto readOneDto = productService.getProductInfo(null, productId);
        log.info("auth x - readOneDto.getUserId() " + readOneDto.getUserId());
        ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                .message("user_product")
                .status(200)
                .data(readOneDto).build();
        return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
    }

    // 전체 물건 조회
    @GetMapping("/all")
    public ResponseEntity<?> getAllProducts(
            Authentication authentication,
            @PageableDefault(size = DEFAULT_PAGE_SIZE)
            @SortDefault(sort = "createDatetime", direction = Sort.Direction.DESC) Pageable pageable){

        if(authentication != null){
            UserDetailsImpl userDetails = (UserDetailsImpl)authentication.getPrincipal();
            ShopProductListDto shopProductListDto = productService.getAllProducts(userDetails, pageable);
            ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                    .message("전체 물건 조회")
                    .status(200)
                    .data(shopProductListDto).build();
            return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
        }
        ShopProductListDto shopProductListDto = productService.getAllProducts(null, pageable);
        ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                .message("전체 물건 조회")
                .status(200)
                .data(shopProductListDto).build();
        return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
    }

    // 좋아요 기능
    @PatchMapping("/like")
    public ResponseEntity<?> cancelLikeProduct(
            Authentication authentication,
            @Valid @RequestBody LikeRequest likeRequest, Errors errors) throws CustomControllerExecption {
        if (authentication == null) throw new CustomControllerExecption("로그인이 필요한 서비스", HttpStatus.UNAUTHORIZED);
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

    @GetMapping
    public void searchProduct(
            Authentication authentication,
            @RequestParam String q,
            @RequestParam(required = false) int category_id){
    }

}
