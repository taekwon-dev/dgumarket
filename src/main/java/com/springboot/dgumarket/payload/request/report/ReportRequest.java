package com.springboot.dgumarket.payload.request.report;

import com.springboot.dgumarket.dto.product.ProductCategoryDto;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

import javax.validation.constraints.NotBlank;
import java.util.Set;

@Getter
@Setter
public class ReportRequest {

    private int report_category_id; // 신고 카테고리

    private String report_etc_reason; // 기타 이유

    @NotBlank
    private JsonNullable<Integer> report_product_id = JsonNullable.undefined();

    @NotBlank
    private JsonNullable<Integer> report_room_id = JsonNullable.undefined();
}
