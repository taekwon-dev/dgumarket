package com.springboot.dgumarket.admin.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ReportResultDto { // 신고처리완료시 응답내용
    int report_id; // 신고접수번호
    int report_result_id; // 신고처리결과번호( 바로 신고처리 결과화면으로 갈 수 있도록 )
    int report_result_type; // 신고처리결과상태 ( 없음, 경고, 제재, 물건블라인드 )
    LocalDateTime report_completed_date;  // 신고 처리 완료일
    int report_status; // 현재 신고에 대한 처리 상태
}
