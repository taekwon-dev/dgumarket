package com.springboot.dgumarket.admin.controller;

import com.springboot.dgumarket.admin.dto.AdminOnlyResultDto;
import com.springboot.dgumarket.admin.dto.ReportResultDto;
import com.springboot.dgumarket.admin.dto.ReportStatusDto;
import com.springboot.dgumarket.admin.request.ProcessReport;
import com.springboot.dgumarket.admin.request.ReportStatusRq;
import com.springboot.dgumarket.admin.service.AdminReportService;
import com.springboot.dgumarket.exception.CustomControllerExecption;
import com.springboot.dgumarket.payload.response.ApiResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminReportController {

    private static final Logger logger = LoggerFactory.getLogger(AdminReportController.class);


    @Autowired
    AdminReportService adminReportService;

    // 신고 접수사항 조회
    @GetMapping("/reports")
    public ResponseEntity<?> getReports(Authentication authentication){
        if(authentication != null) {
            ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                    .message("신고 접수사항 조회")
                    .status(200)
                    .data(adminReportService.getReports())
                    .build();
            return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
        }
        return null;
    }

    // 관리자 신고처리현황 조회
    @GetMapping("/report-results")
    public ResponseEntity<?> getReportResults(Authentication authentication){
        if(authentication != null){
            ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                    .message("관리자 신고처리현황 조회")
                    .status(200)
                    .data(adminReportService.getReportResult())
                    .build();
            return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
        }
        return null;
    }

    // [관리자] 유저경고주기 및 취소하기
    @PutMapping("/alert/{userId}")
    public ResponseEntity<?> doWarn(
            Authentication authentication,
            @RequestBody ProcessReport processRequest,
            @PathVariable("userId") int userId){
        if(authentication != null){
            AdminOnlyResultDto adminOnlyResultDto = adminReportService.warn(userId, processRequest);

            if(adminOnlyResultDto.is_cancel_result()){
                ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                        .status(200)
                        .message("유저 경고 취소하기")
                        .data(adminOnlyResultDto)
                        .build();
                return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
            }else {
                ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                        .message("유저 경고 주기")
                        .status(200)
                        .data(adminOnlyResultDto)
                        .build();
                return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
            }
        }
        return null;
    }

    // [관리자] 물건블라인드 처리하기 및 취소하기
    @PutMapping("/blind/{productId}")
    public ResponseEntity<?> doBlind(
            Authentication authentication,
            @RequestBody ProcessReport processRequest,
            @PathVariable("productId") int productId) throws CustomControllerExecption {
        if(authentication != null){
            logger.info("로그인유저!");
            AdminOnlyResultDto adminOnlyResultDto = adminReportService.blind(productId, processRequest);
            if (adminOnlyResultDto.is_cancel_result()){ // 블라인드 취소요청일 경우
                ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                        .status(200)
                        .message("유저 물건 블라인드 취소하기")
                        .data(adminOnlyResultDto)
                        .build();

                return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
            }else{
                ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                        .status(200)
                        .message("유저 물건 블라인드하기")
                        .data(adminOnlyResultDto)
                        .build();
                return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
            }
        }
        return null;
    }

    // [관리자] 유저제재 가하기
    @PutMapping("/sanction/{userId}")
    public ResponseEntity<?> doSanction(
            Authentication authentication,
            @RequestBody ProcessReport processReport,
            @PathVariable("userId") int userId){

        if(authentication != null){
            logger.info("로그인유저!");
            AdminOnlyResultDto adminOnlyResultDto = adminReportService.sanction(userId, processReport);

            if(adminOnlyResultDto.is_cancel_result()){
                ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                        .status(200)
                        .message("유저제재 취소하기")
                        .data(adminOnlyResultDto)
                        .build();

                return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
            }else{
                ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                        .status(200)
                        .message("유저제재 요청하기")
                        .data(adminOnlyResultDto)
                        .build();

                return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
            }

        }
        return null;
    }



    // 신고접수건에 대해서 처리하기
    @PostMapping("/report/{reportId}/result")
    public ResponseEntity<?> processReport(
            Authentication authentication,
            @PathVariable("reportId") int reportId,
            @RequestBody ProcessReport processRequest){

        if(authentication != null){
            ReportResultDto reportResultDto = adminReportService.processReport(reportId, processRequest);
            ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                    .status(200)
                    .message("신고처리에대한 결과")
                    .data(reportResultDto).build();
            return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
        }
        return null;
    }

    // 신고접수건의 상태 바꾸기 3/8
    @PutMapping("/report/{reportId}/status")
    public ResponseEntity<?> changeReportStatus(
            Authentication authentication,
            @PathVariable("reportId") int reportId,
            @RequestBody ReportStatusRq statusRq){
        if(authentication != null){
            ReportStatusDto reportStatusDto = adminReportService.changeReportStatus(reportId, statusRq);
            ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                    .message("신고 접수 처리상태 바꾸기")
                    .status(200)
                    .data(reportStatusDto)
                    .build();
            return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
        }
        return null;
    }
}
