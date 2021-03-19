package com.springboot.dgumarket.admin.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Builder
public class AdminOnlyResultDto {
    ReportUserDto user; // 유저정보
    int report_result_id; // 처리내역 고유 아이디
    @JsonInclude(JsonInclude.Include.NON_NULL)
    int blind_product_id; // 블라인드 처리된 물건 번호
    int is_report_result; // 신고처리결과에 의한 것 유무 ( 0: 단독관리자처리, 1: 신고접수에 따른 처리 )
    String warning_reason_admin; // 관리자가 따로 작성하는 경고에 대한 이유
    String warning_reason_target; // 제재당한 유저에게 보내는 사유
    String warning_reason_public; // 신고접수한 유저에게 보내는 신고제재처리에 대한 사유
    @JsonInclude(JsonInclude.Include.NON_NULL)
    boolean is_cancel_result; // 취소에 대한 이벤트( 관리자가 취소이벤트 요청 -> true)
}
