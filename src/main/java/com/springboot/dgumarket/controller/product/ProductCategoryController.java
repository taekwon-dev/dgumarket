package com.springboot.dgumarket.controller.product;

import com.springboot.dgumarket.dto.shop.ShopProductListDto;
import com.springboot.dgumarket.exception.CustomControllerExecption;
import com.springboot.dgumarket.payload.request.PagingIndexRequest;
import com.springboot.dgumarket.payload.response.ApiResponseEntity;
import com.springboot.dgumarket.payload.response.ProductListIndex;
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
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@RestController()
@RequestMapping("/api/category")
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

    @PostMapping("/index")
    public ResponseEntity<List<ProductListIndex>> findIndexTest(Authentication authentication, @RequestBody PagingIndexRequest lastCategoryId) {

        if (authentication != null) {
            log.info("로그인");
            // 로그인 상태 (-> '유저의 관심' 카테고리)
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            List<ProductListIndex> productCategorySet = productService.indexLoggedIn(userDetails, lastCategoryId.getLastCategoryId());
            return new ResponseEntity<>(productCategorySet, HttpStatus.OK);
        } else {
            log.info("비로그인");
            // 비로그인 상태 (-> '인기' 카테고리)
            List<ProductListIndex>  products = productService.indexNotLoggedIn(lastCategoryId.getLastCategoryId());
            return new ResponseEntity<>(products, HttpStatus.OK);
        }
    }

    // 카테고리별 물건들 조회
    @GetMapping("/{categoryId}/products")
    @CategoryCheck
    public ResponseEntity<?> getCategoryProducts(
            Authentication authentication,
            @PathVariable(value = "categoryId", required = false) int categoryId,
            @PageableDefault(size = DEFAULT_PAGE_SIZE)
            @SortDefault(sort = "createDatetime", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) @Nullable Integer except_pid) throws CustomControllerExecption {

        if(authentication != null){
            UserDetailsImpl userDetails = (UserDetailsImpl)authentication.getPrincipal();
            ShopProductListDto categoryProducts  = productService.getProductsByCategory(userDetails, categoryId, pageable, except_pid);
            ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                    .message(categoryName[categoryId-1] + " 조회")
                    .status(200)
                    .data(categoryProducts).build();
            return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
        }

        ShopProductListDto categoryProducts = productService.getProductsByCategory(null, categoryId, pageable, except_pid);
        ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                .message(categoryName[categoryId-1] + " 조회")
                .status(200)
                .data(categoryProducts).build();
        return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
    }
}
