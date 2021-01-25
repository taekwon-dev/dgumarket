package com.springboot.dgumarket.controller.product;

import com.springboot.dgumarket.dto.product.ProductCreateDto;
import com.springboot.dgumarket.payload.request.PagingIndexRequest;
import com.springboot.dgumarket.payload.response.ProductListIndex;
import com.springboot.dgumarket.service.UserDetailsImpl;
import com.springboot.dgumarket.service.product.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
}
