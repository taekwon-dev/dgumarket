package com.springboot.dgumarket.model.product;

import com.springboot.dgumarket.model.member.Member;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Created by TK YOUN (2020-12-22 오후 10:04)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 *
 * 02-28 ms
 * 채팅방수, 좋아요 수 -> count 쿼리를 통해 개수를 가져오도록 변경
 * 테스트확인
 *
 */
@Slf4j
@Entity
@Table(name = "product")
@NoArgsConstructor
@Getter
@Setter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    // member_id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", referencedColumnName = "id")
    private Member member;

    // 상품 카테고리
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    private ProductCategory productCategory;

    // 거래 상태 코드
    private int transactionStatusId;

    // 거래 방식 코드
    private int transactionModeId;

    // 상품 타이틀
    private String title;

    // 상품 정보
    private String information;

    // 상품 가격
    private String price;

    // 좋아요 수
    @Formula("(select count(*) from product_like p where p.product_id = id)")
    @Basic(fetch=FetchType.LAZY)
    private int likeNums;

    // 조회 수
    private int viewNums;

    // 개설된 채팅방 수
    @Formula("(select count(*) from chat_room cm where cm.product_id = id)")
    @Basic(fetch=FetchType.LAZY)
    private int chatroomNums;

    // 가격 조정 여부
    private int isNego;

    // 상품 상태
    private int productStatus;

    // 판매자 자체 상품 등급 평가
    private int selfProductStatus;

    // 업로드한 이미지 저장 디렉토리
    private String imgDirectory;

    // 업로드 시간
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createDatetime;

    // 수정 시간
    @UpdateTimestamp
    private LocalDateTime updateDatetime;
}
