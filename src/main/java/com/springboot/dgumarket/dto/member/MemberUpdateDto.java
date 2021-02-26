package com.springboot.dgumarket.dto.member;

import com.springboot.dgumarket.dto.product.ProductCategoryDto;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

import javax.validation.constraints.NotBlank;
import java.util.Set;

/**
 * Created by TK YOUN (2021-01-01 오후 4:08)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 *
 * 회원정보 및 수정
 */

@Getter
@Setter
public class MemberUpdateDto {

    private JsonNullable<String> profileImageDir = JsonNullable.undefined();
    private JsonNullable<String> nickName = JsonNullable.undefined();
    private JsonNullable<Set<ProductCategoryDto>> productCategories = JsonNullable.undefined();

}
