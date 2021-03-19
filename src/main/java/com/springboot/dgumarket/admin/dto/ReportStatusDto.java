package com.springboot.dgumarket.admin.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Builder
public class ReportStatusDto { // 신고 처리상태값 바꿀때 응답값(신고대기 -> 신고접수)
    int report_id; // 신고고유아이디
    int report_current_status; // 현재 신고의 접수상태
    LocalDateTime report_status_date; // 신고 처리된일
}
