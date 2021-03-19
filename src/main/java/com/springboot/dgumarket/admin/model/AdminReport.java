package com.springboot.dgumarket.admin.model;

import com.springboot.dgumarket.admin.dto.AdminReportDto;
import com.springboot.dgumarket.admin.dto.ReportProductDto;
import com.springboot.dgumarket.admin.dto.ReportUserDto;
import com.springboot.dgumarket.model.chat.ChatRoom;
import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.model.product.Product;
import com.springboot.dgumarket.model.report.ReportCategory;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@DynamicUpdate
@Entity
@Table(name = "report")
public class AdminReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Member.class)
    @JoinColumn(name = "reporter_id", referencedColumnName = "id")
    private Member reporter; // 신고자

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Member.class)
    @JoinColumn(name = "target_id", referencedColumnName = "id")
    private Member targetUser; // 신고 대상자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_category", referencedColumnName = "id")
    private ReportCategory reportCategory; // 신고 카테고리

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_product_id", referencedColumnName = "id")
    private Product reportProduct; // 신고 물건( optional )

    @Column(name = "report_image_dir")
    private String reportImages; // 신고 이미지 경로


    @Column(name = "report_content")
    private String reportEtcReason; // 신고 기타 이유

    @Column(name = "report_status")
    private int reportStatus; // 신고처리 상태

    @CreationTimestamp
    @Column(name = "report_date", updatable = false)
    private LocalDateTime reportDate; // 신고 일

    @Column(name = "report_reception_date")
    private LocalDateTime reportReceptionDate; // 신고처리 접수일

    @Column(name = "report_completed_date")
    private LocalDateTime reportCompletedDate; // 신고처리 완료일


    public AdminReportDto toDto(){

        ReportUserDto targetUserDto = ReportUserDto.builder()
                .user_id(targetUser.getId())
                .user_nickname(targetUser.getNickName()).build();

        ReportUserDto reportUserDto = ReportUserDto.builder()
                .user_id(reporter.getId())
                .user_nickname(reporter.getNickName()).build();

        AdminReportDto adminReportDto = AdminReportDto.builder()
                .id(this.id)
                .report_type(this.reportCategory.getCategoryName())
                .report_type_id(this.reportCategory.getId())
                .reporter(reportUserDto) // 신고자
                .report_target(targetUserDto) // 신고대상자
                .report_etc_reason(this.reportEtcReason) // 신고 기타 이유
                .report_image_dir(this.reportImages) // 신고 이미지 경로
                .report_date(this.reportDate) // 유저 신고일
                .report_reception_date(this.reportReceptionDate) // 신고 접수일(관리자)
                .report_completed_date(this.reportCompletedDate) // 신고처리완료일
                .report_status(this.reportStatus).build(); // 신고처리 상태

        if(this.getReportProduct() != null){
            ReportProductDto reportProductDto = ReportProductDto.builder()
                    .report_product_description(this.reportProduct.getInformation())
                    .report_product_title(this.reportProduct.getTitle())
                    .report_product_imgs(this.reportImages).build();
            adminReportDto.setReportProductDto(reportProductDto);
        }

        return adminReportDto;
    }
}
