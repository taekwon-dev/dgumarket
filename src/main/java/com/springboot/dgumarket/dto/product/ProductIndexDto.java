package com.springboot.dgumarket.dto.product;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by TK YOUN (2020-12-28 오후 8:03)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */

@Getter
@Setter
public class ProductIndexDto {
    private int category_id;
    private String category_name;
    private List<ProductReadListDto> productsList;
}
