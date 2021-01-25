package com.springboot.dgumarket.model.report;

import com.springboot.dgumarket.model.chat.ChatRoom;
import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.model.product.Product;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "report")
public class Report {
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
    private Product reportProduct;


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

    @Column(name = "report_completed_content")
    private String reportCompletedContent; // 신고처리결과내용

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = ChatRoom.class)
    @JoinColumn(name = "report_chatroom", referencedColumnName = "id")
    private ChatRoom chatRoom;

}
