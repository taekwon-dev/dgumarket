package com.springboot.dgumarket.payload.request.report;

import com.springboot.dgumarket.dto.product.ProductCategoryDto;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.security.core.Authentication;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Set;

/**
 * created by ms, 2021-01-21
 *  사용자 신고 요청 BODY 에 아래의 값들을 포함하여 요청
 * {@link com.springboot.dgumarket.controller.report.ReportController#postReport(ReportRequest, Authentication)}  참고}
 */
@Getter
@Setter
public class ReportRequest {

    @NotEmpty(message = "require report_category_id")
    private int report_category_id; // 신고 카테고리

    @NotBlank(message = "require etc_reason")
    private String report_etc_reason; // 기타 이유

    @NotBlank
    private JsonNullable<Integer> report_product_id = JsonNullable.undefined();

    @NotBlank
    private JsonNullable<Integer> report_room_id = JsonNullable.undefined();

    @NotBlank
    private JsonNullable<String> report_img_path = JsonNullable.undefined();
}
