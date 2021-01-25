package com.springboot.dgumarket.service.product;

import com.springboot.dgumarket.dto.product.ProductReviewDto;
import com.springboot.dgumarket.payload.request.ProductCommentRequset;


/**
 * Created by MS KIM (2020-01-15)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 *
 * addProductComment -> 거래 후기 남기기(구매자)
 * getProductComment -> 거래 후기 보기(구매자, 판매자)
 */

public interface ProductReviewService {
    void addProductComment(int productId, int userId, ProductCommentRequset commentRequset);
    ProductReviewDto getProductComment(int productId, int userId);
}
