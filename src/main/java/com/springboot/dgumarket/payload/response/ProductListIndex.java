package com.springboot.dgumarket.payload.response;

import com.springboot.dgumarket.dto.product.ProductReadListDto;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Created by TK YOUN (2020-12-28 오후 12:36)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */

@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class ProductListIndex {

    private int category_id;
    private String category_name;
    private List<ProductReadListDto> productsList;
}
