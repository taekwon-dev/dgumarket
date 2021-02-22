package com.springboot.dgumarket.controller.product;

import com.springboot.dgumarket.dto.shop.ShopProductListDto;
import com.springboot.dgumarket.payload.response.ApiResponseEntity;
import com.springboot.dgumarket.service.UserDetailsImpl;
import com.springboot.dgumarket.service.product.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class ProductCategoryController {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final String[] categoryName = {
            "도서",
            "음반/DVD",
            "뷰티",
            "기프티콘",
            "가전/디지털",
            "식품",
            "완구/취미",
            "주방용품",
            "생활용품",
            "홈 인테리어",
            "스포츠/레저",
            "반려동물 용품",
            "문구/오피스",
            "의류/잡화",
            "기타"
    };

    @Autowired
    ProductService productService;

    // 카테고리별 물건들 조회
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<?> getCategoryProducts(
            Authentication authentication,
            @PathVariable(value = "categoryId", required = false) int categoryId,
            @PageableDefault(size = DEFAULT_PAGE_SIZE)
            @SortDefault(sort = "createDatetime", direction = Sort.Direction.DESC) Pageable pageable){

        if(authentication != null){
            UserDetailsImpl userDetails = (UserDetailsImpl)authentication.getPrincipal();
            ShopProductListDto categoryProducts  = productService.getProductsByCategory(userDetails, categoryId, pageable);
            ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                    .message(categoryName[categoryId-1] + " 조회")
                    .status(200)
                    .data(categoryProducts).build();
            return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
        }

        ShopProductListDto categoryProducts = productService.getProductsByCategory(null, categoryId, pageable);
        ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                .message(categoryName[categoryId-1] + " 조회")
                .status(200)
                .data(categoryProducts).build();
        return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
    }

    // 전체 물건 조회
    @GetMapping("/products")
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
}
