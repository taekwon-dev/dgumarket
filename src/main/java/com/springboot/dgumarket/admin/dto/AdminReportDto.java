package com.springboot.dgumarket.admin.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.springboot.dgumarket.model.report.Report;
import lombok.*;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;


// dto 이지만 우선 여러 데이터를 탐색할 수 있도록 기본 유저의 id , 물건이면 물건의 id 를 포함해서 내려주도록 하자
@Getter
@Setter
@Builder
@AllArgsConstructor
public class AdminReportDto {

    int id; // 신고번호
    int report_type_id; // 신고카테고리 id
    String report_type; // 신고카테고리
    ReportUserDto reporter; // 신고자
    ReportUserDto report_target; // 신고대상자
    String report_etc_reason; // 신고 이유
    String report_image_dir; // 신고이미지경로
    @JsonInclude(JsonInclude.Include.NON_NULL)
    ReportProductDto reportProductDto;
    int report_status; // 신고처리상태
    LocalDateTime report_date; // 신고일
    LocalDateTime report_reception_date; // 신고처리 접수일
    LocalDateTime report_completed_date; // 신고처리 완료일
}
