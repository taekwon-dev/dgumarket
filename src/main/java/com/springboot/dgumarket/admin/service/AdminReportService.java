package com.springboot.dgumarket.admin.service;

import com.springboot.dgumarket.admin.dto.*;
import com.springboot.dgumarket.admin.request.ProcessReport;
import com.springboot.dgumarket.admin.request.ReportStatusRq;
import com.springboot.dgumarket.exception.CustomControllerExecption;

import java.util.List;

public interface AdminReportService {

    List<AdminReportDto> getReports(); // 유저 신고 조회
    List<AdminReportResultDto> getReportResult(); // 관리자 신고 처리 내역 조회

    ReportResultDto processReport(int reportId, ProcessReport processRequest); // 신고접수건에 대한 처리
    ReportStatusDto changeReportStatus(int reportId, ReportStatusRq statusRq); // 신고접수건 상태바꾸기

    AdminOnlyResultDto warn(int userId, ProcessReport processRequest); // [관리자] 유저 경고 및 경고 취소하기
    AdminOnlyResultDto sanction(int userId, ProcessReport processRequest); // [관리자] 유저 제재 및 제재 취소하기
    AdminOnlyResultDto blind(int productId, ProcessReport processRequest) throws CustomControllerExecption; // [관리자] 유저 물건 블라인드 처리 및 블라인드 처리 취소하기

}
