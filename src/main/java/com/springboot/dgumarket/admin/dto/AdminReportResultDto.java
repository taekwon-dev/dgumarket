package com.springboot.dgumarket.admin.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
@Builder
@AllArgsConstructor
public class AdminReportResultDto {
    int report_result_id; // 신고처리결과의 고유 번호
    int report_id; // 신고에 대한 처리일 경우, 유저의 신고고유 번호
    ReportUserDto user; // 유저정보
    @JsonInclude(JsonInclude.Include.NON_NULL)
    ReportProductDto blindProduct; // 블라인드 처리된 물건
    int is_report_result; // 신고처리결과에 의한 것 유무 ( 0: 단독관리자처리, 1: 신고접수에 따른 처리 )
    String warning_reason_admin; // 관리자가 따로 작성하는 경고에 대한 이유
    String warning_reason_target; // 제재당한 유저에게 보내는 사유
    String warning_reason_public; // 신고접수한 유저에게 보내는 신고제재처리에 대한 사유
}
