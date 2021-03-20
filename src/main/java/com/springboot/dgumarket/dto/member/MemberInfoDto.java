package com.springboot.dgumarket.dto.member;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.springboot.dgumarket.dto.product.ProductCategoryDto;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by TK YOUN (2021-01-02 오후 6:28)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */
@Getter
@Setter
public class MemberInfoDto {
    private String profileImageDir;
    private String nickName;
    private Set<ProductCategoryDto> productCategories = new HashSet<>();
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private boolean warn; // 경고유무
}
