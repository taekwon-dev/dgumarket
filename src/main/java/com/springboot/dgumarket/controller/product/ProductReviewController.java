package com.springboot.dgumarket.controller.product;

import com.springboot.dgumarket.dto.product.ProductReviewDto;
import com.springboot.dgumarket.payload.request.review.ProductCommentRequset;
import com.springboot.dgumarket.payload.response.ApiResponseEntity;
import com.springboot.dgumarket.service.UserDetailsImpl;
import com.springboot.dgumarket.service.product.ProductReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/product")
public class ProductReviewController {

    @Autowired
    ProductReviewService productReviewService;

    // 구매후기 남기기
    @PostMapping("/{product-id}/comment")
    public ResponseEntity<?> writeComment(
            @PathVariable("product-id") int productId,
            @RequestBody ProductCommentRequset commentRequest,
            Authentication authentication){

        if (authentication != null){
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            productReviewService.addProductComment(productId, userDetails.getId(), commentRequest);

            // location 업데이트된 자원의 위치를 알려준다.
            URI location = URI.create("/api/product/" + productId + "/comment");
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setLocation(location);
            return new ResponseEntity<>("Posted successfully", responseHeaders, HttpStatus.CREATED);
        }

        return null;
    }


    // 구매후기 보기
    @GetMapping("/{product-id}/comment")
    public ResponseEntity<?> getProductComment(@PathVariable("product-id") int productId, Authentication authentication){

        if (authentication != null){
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            ProductReviewDto productReviewDto = productReviewService.getProductComment(productId, userDetails.getId());

            return new ResponseEntity<>(
                    ApiResponseEntity.builder()
                            .status(200)
                            .message("product comment")
                            .data(productReviewDto).build(),
                    HttpStatus.OK
            );
        }

        return null;
    }
}
