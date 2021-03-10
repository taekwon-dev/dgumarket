package com.springboot.dgumarket.admin.service;


import com.amazonaws.services.dynamodbv2.xspec.B;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.springboot.dgumarket.admin.dto.*;
import com.springboot.dgumarket.admin.model.AdminReport;
import com.springboot.dgumarket.admin.model.AdminReportResult;
import com.springboot.dgumarket.admin.repository.AdminReportRepository;
import com.springboot.dgumarket.admin.repository.AdminReportResultRepository;
import com.springboot.dgumarket.admin.request.ProcessReport;
import com.springboot.dgumarket.admin.request.ReportStatusRq;
import com.springboot.dgumarket.exception.CustomControllerExecption;
import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.model.member.User;
import com.springboot.dgumarket.model.product.Product;
import com.springboot.dgumarket.model.report.ReportResult;
import com.springboot.dgumarket.repository.member.MemberRepository;
import com.springboot.dgumarket.repository.member.UserRepository;
import com.springboot.dgumarket.repository.product.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class AdminReportServiceImpl implements AdminReportService{


    private static final int NONE = 0;
    private static final int WARN = 1;
    private static final int SACTION = 2;
    private static final int BLIND = 3;


    private static final int COMPLETE = 2;
    private static final int RECEPTION = 1;

    @Autowired
    AdminReportRepository adminReportRepository;

    @Autowired
    AdminReportResultRepository adminReportResultRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    UserRepository userRepository;

    // 신고한 내역 조회하기
    @Override
    public List<AdminReportDto> getReports() {
        return adminReportRepository.findAll()
                .stream()
                .map(AdminReport::toDto).collect(Collectors.toList());
    }


    // 신고 처리 결과 조회하기
    @Override
    public List<AdminReportResultDto> getReportResult() {
        return adminReportResultRepository.findAll()
                .stream()
                .map(AdminReportResult::toDto).collect(Collectors.toList());
    }

    // 신고접수건에 대해서 신고처리하기
    @Override
    public ReportResultDto processReport(int reportId, ProcessReport processRequest) {
        AdminReport userReport = adminReportRepository.getOne(reportId);
        ReportResultDto reportResultDto;
        AdminReportResult reportResult;
        switch (processRequest.getProcess_type()){
            case NONE:
                // 신고처리결과 내역 저장
                reportResult = adminReportResultRepository.save(AdminReportResult.builder()
                        .userReport(userReport)
                        .isReportResult(1)
                        .reportResultType(NONE) // 아무것도 아님
                        .member(userReport.getTargetUser())
                        .warningReasonAdmin(processRequest.getAdmin_content())
                        .warningReasonPublic(processRequest.getPublic_content())
                        .warningReasonTarget(processRequest.getTarget_content())
                        .create_datetime(LocalDateTime.now())
                        .build());
                userReport.setReportStatus(COMPLETE); // 신고접수데이터의 신고처리상태를 완료로 바꾸기


                reportResultDto = ReportResultDto.builder()
                        .report_id(reportId)
                        .report_result_id(reportResult.getId())
                        .report_result_id(reportResult.getId())
                        .report_completed_date(reportResult.getCreate_datetime())
                        .report_result_type(NONE)
                        .report_status(COMPLETE).build();

                // 문자메시지 보내기
                break;
            case WARN: // 경고
                userReport.getTargetUser().addWarn(); // 유저에게 경고 횟수 추가 & 일자 갱신
                reportResult = adminReportResultRepository.save(AdminReportResult.builder() // 신고내역 저장
                        .userReport(userReport) // 신고내역
                        .isReportResult(1) // 신고처리에 대한 결과
                        .reportResultType(WARN) // 경고
                        .member(userReport.getTargetUser())
                        .warningReasonAdmin(processRequest.getAdmin_content())
                        .warningReasonPublic(processRequest.getPublic_content())
                        .warningReasonTarget(processRequest.getTarget_content())
                        .build());
                userReport.setReportStatus(2); // 신고 처리결과 완료로 바꿈

                // 문자메시지보내기


                reportResultDto = ReportResultDto.builder()
                        .report_id(reportId)
                        .report_result_id(reportResult.getId())
                        .report_result_id(reportResult.getId())
                        .report_completed_date(reportResult.getCreate_datetime())
                        .report_result_type(WARN)
                        .report_status(COMPLETE).build();


                break;
            case SACTION: // 제재

                // 유저제재가하기
                userReport.getTargetUser().punish();
                User user = userRepository.findById(userReport.getId());
                if(user != null){
                    user.punish();
                }


                reportResult = adminReportResultRepository.save(AdminReportResult.builder() // 신고내역 저장
                        .userReport(userReport) // 신고내역
                        .isReportResult(1) // 유저의 신고에대해서 처리할 경우
                        .reportResultType(SACTION) // 경고
                        .member(userReport.getTargetUser())
                        .warningReasonAdmin(processRequest.getAdmin_content())
                        .warningReasonPublic(processRequest.getPublic_content())
                        .warningReasonTarget(processRequest.getTarget_content())
                        .build());
                userReport.setReportStatus(2); // 신고 처리결과 완료로 바꿈

                // 문자메시지보내기



                reportResultDto = ReportResultDto.builder()
                        .report_id(reportId)
                        .report_result_id(reportResult.getId())
                        .report_result_id(reportResult.getId())
                        .report_completed_date(reportResult.getCreate_datetime())
                        .report_result_type(SACTION)
                        .report_status(COMPLETE).build();

                break;
            case BLIND: // 블라인드
                Product product = productRepository.getOne(processRequest.getBlind_product_id());
                if(product!=null){
                    product.setProductStatus(2); // 해당 물건 블라인드 처리하기
                }
                reportResult = adminReportResultRepository.save(AdminReportResult.builder() // 신고내역 저장
                        .userReport(userReport) // 신고내역
                        .isReportResult(1) // 유저의 신고에대해서 처리할 경우
                        .reportResultType(BLIND) // 경고
                        .member(userReport.getTargetUser())
                        .warningReasonAdmin(processRequest.getAdmin_content())
                        .warningReasonPublic(processRequest.getPublic_content())
                        .warningReasonTarget(processRequest.getTarget_content())
                        .build());
                userReport.setReportStatus(2); // 신고 처리결과 완료로 바꿈

                reportResultDto = ReportResultDto.builder()
                        .report_result_id(reportResult.getId())
                        .report_id(reportId)
                        .report_result_id(reportResult.getId())
                        .report_completed_date(reportResult.getCreate_datetime())
                        .report_result_type(SACTION)
                        .report_status(COMPLETE).build();

                // 문자메시지 보내기
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + processRequest.getProcess_type());
        }

        return reportResultDto;
    }



    // ------------------------------- [ 관리자 ] ---------------------------------------

    // [관리자] 경고하기 및 경고 취소하기
    @Override
    public AdminOnlyResultDto warn(int targetUserId, ProcessReport processRequest) {
        Member member = memberRepository.findById(targetUserId);

        AdminReportResult adminResult = null;
        if(processRequest.getIs_cancel()==1 && processRequest.getProcess_type()==WARN){ // 경고취소요청일 경우
            member.cancelWarn(); // 유저경고 취소하기
            adminResult = adminReportResultRepository.save(AdminReportResult.builder() // 처리결과 저장
                    .isReportResult(0) // 관리자 단독 처리
                    .reportResultType(WARN)
                    .isAlertCancel(1) // 경고취소처리
                    .warnCancelDate(LocalDateTime.now()) // 경고취소일 등록
                    .create_datetime(LocalDateTime.now())
                    .member(member)
                    .warningReasonAdmin(processRequest.getAdmin_content())
                    .warningReasonPublic(processRequest.getPublic_content())
                    .warningReasonTarget(processRequest.getTarget_content()).build());

            ReportUserDto reportUserDto = ReportUserDto.builder()
                    .user_id(adminResult.getMember().getId())
                    .user_nickname(adminResult.getMember().getNickName()).build();

            // 문자보내기 로직 들어감(경고취소당한 사람에게)

            return AdminOnlyResultDto.builder()
                    .report_result_id(adminResult.getId())
                    .is_report_result(0) // 신고처리결과에 대한 내용이 아님
                    .warning_reason_admin(adminResult.getWarningReasonAdmin())
                    .warning_reason_target(adminResult.getWarningReasonTarget())
                    .user(reportUserDto)
                    .is_cancel_result(true).build(); // 취소이벤트에 대한 처리이다

        }else if(processRequest.getProcess_type()==WARN){
            member.addWarn(); // 유저경고주기
            adminResult = adminReportResultRepository.save(AdminReportResult.builder() // 처리결과 저장
                    .isReportResult(0) // 관리자 단독 처리
                    .reportResultType(WARN)
                    .member(member)
                    .warningReasonAdmin(processRequest.getAdmin_content())
                    .warningReasonPublic(processRequest.getPublic_content())
                    .warningReasonTarget(processRequest.getTarget_content()).build());

            ReportUserDto reportUserDto = ReportUserDto.builder()
                    .user_id(adminResult.getMember().getId())
                    .user_nickname(adminResult.getMember().getNickName()).build();


            // 문자보내기 로직 들어감(경고당한 사람에게)


            return AdminOnlyResultDto.builder()
                    .report_result_id(adminResult.getId())
                    .is_report_result(0) // 신고처리결과에 대한 내용이 아님
                    .warning_reason_admin(adminResult.getWarningReasonAdmin())
                    .warning_reason_target(adminResult.getWarningReasonTarget())
                    .user(reportUserDto)
                    .is_cancel_result(false).build(); // 취소이벤트에 대한 처리이다
        }

        return null;
    }

    // [관리자] 유저제재 및 제재 취소하기
    @Override
    public AdminOnlyResultDto sanction(int targetUserId, ProcessReport processRequest) {
        Member member = memberRepository.findById(targetUserId);

        if(processRequest.getProcess_type()==SACTION && processRequest.getIs_cancel() == 1){ // 제재취소요청

            member.unPunish(); // 제재취소하기
            User user = userRepository.findById(targetUserId);
            if(user != null){
                user.unPunish();
            }

            // 처리결과 저장
            AdminReportResult adminResult = adminReportResultRepository.save(AdminReportResult.builder()
                    .isReportResult(0) // 관리자 단독 처리
                    .reportResultType(SACTION)
                    .isSactionCancel(1) // 제재취소처리
                    .sactionCancelDate(LocalDateTime.now()) // 제재취소일 등록
                    .create_datetime(LocalDateTime.now())
                    .member(member)
                    .warningReasonAdmin(processRequest.getAdmin_content())
                    .warningReasonPublic(processRequest.getPublic_content())
                    .warningReasonTarget(processRequest.getTarget_content())
                    .build());

            ReportUserDto reportUserDto = ReportUserDto.builder()
                    .user_id(adminResult.getMember().getId())
                    .user_nickname(adminResult.getMember().getNickName()).build();
            //[ 문자메시지 보내기 ]

            return AdminOnlyResultDto.builder()
                    .report_result_id(adminResult.getId())
                    .is_report_result(0) // 신고처리결과에 대한 내용이 아님
                    .warning_reason_admin(adminResult.getWarningReasonAdmin())
                    .warning_reason_target(adminResult.getWarningReasonTarget())
                    .user(reportUserDto)
                    .is_cancel_result(true).build(); // 취소이벤트에 대한 처리이다


        }else if(processRequest.getProcess_type()==SACTION){

            // 제재하기
            member.punish();
            User user = userRepository.findById(targetUserId);
            if(user != null){
                user.punish();
            }


            // 처리결과 저장
            AdminReportResult adminResult = adminReportResultRepository.save(AdminReportResult.builder()
                    .isReportResult(0) // 관리자 단독 처리
                    .reportResultType(SACTION)
                    .member(member)
                    .warningReasonAdmin(processRequest.getAdmin_content())
                    .warningReasonPublic(processRequest.getPublic_content())
                    .warningReasonTarget(processRequest.getTarget_content())
                    .build());
            ReportUserDto reportUserDto = ReportUserDto.builder()
                    .user_id(adminResult.getMember().getId())
                    .user_nickname(adminResult.getMember().getNickName()).build();

            //[ 문자메시지 보내기 ]

            return AdminOnlyResultDto.builder()
                    .report_result_id(adminResult.getId())
                    .is_report_result(0) // 신고처리결과에 대한 내용이 아님
                    .warning_reason_admin(adminResult.getWarningReasonAdmin())
                    .warning_reason_target(adminResult.getWarningReasonTarget())
                    .user(reportUserDto)
                    .is_cancel_result(false).build(); // 취소이벤트에 대한 처리이다
        }
        return null;
    }

    // [관리자] 물건 블라인드 처리 및 취소하기
    @Override
    public AdminOnlyResultDto blind(int productId, ProcessReport processRequest) throws CustomControllerExecption {
        Product product = productRepository.getOne(productId);

        AdminReportResult adminResult = null;
        ReportUserDto reportUserDto = null;
        if(processRequest.getIs_cancel()==1 && processRequest.getProcess_type()==BLIND){ // 취소요청

            switch (product.getProductStatus()){
                case 0:
                    throw new CustomControllerExecption("블라인드 상태가 아니기 때문에 블라인드 취소요청을 할 수 없습니다.", HttpStatus.NOT_FOUND);
                case 1:
                    throw new CustomControllerExecption("해당 물건은 이미 유저가 삭제하였습니다.", HttpStatus.NOT_FOUND);
                case 2: // 현재 블라인드 상태라면
                    product.setProductStatus(0); // 블라인드 취소

                    adminResult = adminReportResultRepository.save(AdminReportResult.builder() // 처리결과 저장
                            .isReportResult(0)
                            .reportResultType(BLIND)
                            .isBlindCancel(1) // 블라인드 취소처리
                            .blindCancelDate(LocalDateTime.now()) // 블라인드 취소일 등록
                            .create_datetime(LocalDateTime.now())
                            .blindProduct(product) // 블라인드 된 물건
                            .member(product.getMember())
                            .warningReasonAdmin(processRequest.getAdmin_content())
                            .warningReasonPublic(processRequest.getPublic_content())
                            .warningReasonTarget(processRequest.getTarget_content())
                            .build());

                    reportUserDto = ReportUserDto.builder()
                            .user_id(adminResult.getMember().getId())
                            .user_nickname(adminResult.getMember().getNickName()).build();

                    //[ 문자메시지 보내기 ]

                    return AdminOnlyResultDto.builder()
                            .report_result_id(adminResult.getId()) // 처리결과 번호
                            .blind_product_id(productId) // 블라인드 요청 취소한 물건번호
                            .is_report_result(0) // 신고처리결과에 대한 내용이 아님
                            .warning_reason_admin(adminResult.getWarningReasonAdmin())
                            .warning_reason_target(adminResult.getWarningReasonTarget())
                            .user(reportUserDto) // 블라인드 취소 요청된 물건의 소유자
                            .is_cancel_result(true).build(); // 취소이벤트에 대한 처리가 아니다
            }
        }else if(processRequest.getProcess_type()==BLIND){
            switch (product.getProductStatus()){
                case 0: // 현재 블라인드 상태가 아니라면
                    product.setProductStatus(2); // 블라인드 하기

                    adminResult = adminReportResultRepository.save(AdminReportResult.builder() // 처리결과 저장
                            .isReportResult(0)
                            .reportResultType(BLIND)
                            .blindProduct(product) // 블라인드 된 물건
                            .member(product.getMember())
                            .warningReasonAdmin(processRequest.getAdmin_content())
                            .warningReasonPublic(processRequest.getPublic_content())
                            .warningReasonTarget(processRequest.getTarget_content())
                            .build());

                    reportUserDto = ReportUserDto.builder()
                            .user_id(adminResult.getMember().getId())
                            .user_nickname(adminResult.getMember().getNickName()).build();

                    //[ 문자메시지 보내기 ]

                    return AdminOnlyResultDto.builder()
                            .report_result_id(adminResult.getId())
                            .blind_product_id(productId)
                            .is_report_result(0) // 신고처리결과에 대한 내용이 아님
                            .warning_reason_admin(adminResult.getWarningReasonAdmin())
                            .warning_reason_target(adminResult.getWarningReasonTarget())
                            .user(reportUserDto)
                            .is_cancel_result(false).build(); // 취소이벤트에 대한 처리가 아니다
                case 1:
                    throw new CustomControllerExecption("해당 물건은 이미 유저가 삭제하였습니다.", HttpStatus.NOT_FOUND);
                case 2: // 현재 블라인드 상태라면
                    throw new CustomControllerExecption("이미 블라인드 상태이기 때문에 블라인드 요청을 할 수 없습니다.", HttpStatus.NOT_FOUND);
            }
        }else {
            throw new IllegalArgumentException("잘못된 데이터 값 입력");
        }

        return null;
    }

    // [관리자] 신고접수에 대한 상태바꾸기(접수대기 -> 접수완료)
    @Override
    @Transactional
    public ReportStatusDto changeReportStatus(int reportId, ReportStatusRq statusRq) {

        if(statusRq.getCurrent_status()==0){ // 신고접수대기 중일 때
            AdminReport adminReport = adminReportRepository.getOne(reportId);
            adminReport.setReportStatus(RECEPTION); // 접수처리중 상태로 변경

            // 접수일 날짜 갱신
            LocalDateTime nowDateTime = LocalDateTime.now();
            adminReport.setReportReceptionDate(nowDateTime);

            // [ 문자메시지 ] 보내기

            return ReportStatusDto.builder()
                    .report_current_status(1)
                    .report_id(reportId)
                    .report_status_date(nowDateTime)
                    .build();
        }else{
            throw new IllegalArgumentException("잘못된 데이터 값 입력");
        }
    }
}
