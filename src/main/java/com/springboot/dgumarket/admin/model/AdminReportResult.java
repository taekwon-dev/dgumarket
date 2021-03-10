package com.springboot.dgumarket.admin.model;


import com.springboot.dgumarket.admin.dto.AdminReportResultDto;
import com.springboot.dgumarket.admin.dto.ReportProductDto;
import com.springboot.dgumarket.admin.dto.ReportUserDto;
import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.model.product.Product;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "report_result")
public class AdminReportResult {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id; // 처리번호

    @OneToOne(fetch = FetchType.LAZY, targetEntity = Member.class)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private Member member; // 유저아이디

    @Column(name = "report_result_type")
    private int reportResultType;  // 신고처리종류 ( 0: none, 1: 경고, 2: 제재, 3: 물건블라인드 )

    @Column(name = "warning_reason_admin")
    private String warningReasonAdmin; // 유저가 관리하는 경고사유(보관용)

    @Column(name = "warning_reason_target")
    private String warningReasonTarget; // 조치당하는 유저에게 보내는 사유

    @Column(name = "warning_reason_public")
    private String warningReasonPublic; // 신고자에게 보내는 사유

    @OneToOne(fetch = FetchType.LAZY, targetEntity = Product.class)
    @JoinColumn(name = "blind_product_id", referencedColumnName = "id")
    private Product blindProduct; // 블라인드된 물건 번호

    @Column(name = "is_report_result")
    private int isReportResult; // 유저신고처리에 대한 결과 ( 0: 단독관리자처리, 1: 신고에대한 처리)

    @OneToOne(fetch = FetchType.LAZY, targetEntity = AdminReport.class)
    @JoinColumn(name = "report_id", referencedColumnName = "id")
    private AdminReport userReport; // 신고처리결과

    @Column(name = "create_datetime")
    private LocalDateTime create_datetime; // 신고처리에대한 날짜

    @Column(name = "is_blind_cancel")
    private int isBlindCancel;

    @Column(name = "is_saction_cancel")
    private int isSactionCancel;

    @Column(name = "is_alert_cancel")
    private int isAlertCancel;

    @Column(name = "warn_cancel_date")
    private LocalDateTime warnCancelDate;

    @Column(name = "saction_cancel_date")
    private LocalDateTime sactionCancelDate;

    @Column(name = "blind_cancel_date")
    private LocalDateTime blindCancelDate;




    public AdminReportResultDto toDto(){
        ReportUserDto reportUserDto = ReportUserDto.builder()
                .user_nickname(this.member.getNickName())
                .user_id(this.member.getId()).build();

        AdminReportResultDto adminReportResultDto = AdminReportResultDto.builder()
                .report_result_id(this.id) // 처리결과 번호
                .is_report_result(this.isReportResult) // 신고처리에 대한 결과 인지 관리자가 독단적으로 처리한 결과인지( 1: 신고에 대한 처리결과, 0: 유저 단독 처리결과)
                .user(reportUserDto) // 신고자
                .warning_reason_admin(this.warningReasonAdmin) // 관리자용 글
                .warning_reason_public(this.warningReasonPublic) // 신고자에게 전달하는 글
                .warning_reason_target(this.warningReasonTarget).build(); // 신고당하는 사람에게 전달하는 글

        if(blindProduct != null){ // 블라인드 요청처리 결과 일 경우
            ReportProductDto reportProductDto = ReportProductDto.builder()
                    .report_product_imgs(this.blindProduct.getImgDirectory())
                    .report_product_title(this.blindProduct.getTitle())
                    .product_id(this.blindProduct.getId()).build();
            adminReportResultDto.setBlindProduct(reportProductDto);
        }

        if(userReport != null){// 신고에대한 처리결과일경우
            adminReportResultDto.setReport_id(this.userReport.getId()); // 신고접수번호 넣기
        }

        return adminReportResultDto;
    };
}
