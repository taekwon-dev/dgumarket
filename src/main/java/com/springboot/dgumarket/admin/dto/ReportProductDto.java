package com.springboot.dgumarket.admin.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ReportProductDto {
    private int product_id; // 물건번호
    private String report_product_title; // 물건제목
    private String report_product_description; // 물건 설명
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String report_product_imgs; // 물건 이미지 경로
}
