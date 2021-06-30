package com.springboot.dgumarket.model.product;

import com.springboot.dgumarket.model.member.Member;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;


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

    /** [회원] - Entity Relationship
     *  - 주인 관계 (= 실제 테이블에서 회원 테이블에 대한 조인 컬럼 보유)
     *  - Product : Members = Many : One
     *  - Fetch 옵션 : FetchType.LAZY (지연로딩 우선 적용 후 추후 경과보고 판단)
     *  - Cascade 옵션 : 당장 적용할 근거를 찾지 못함 (= 필요성 재고)
     *    - 상품 정보 저장 또는 삭제 시 회원 테이블에 영향 주는 부분이 없음 (이유)
     * */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", referencedColumnName = "id")
    private Member member;

    /** [상품 카테고리] - Entity Relsationship
     *  - 주인 관계 (= 실제 테이블에서 상품 카테고리 테이블에 대한 조인 컬럼 보유
     *  - Product : ProductCategory = Many to One
     *  - Fetch 옵션 : FetchType.LAZY (지연로딩 우선 적용 후 추후 경과보고 판단)
     *  - Cascade 옵션 : 당장 적용할 근거를 찾지 못함 (= 필요성 재고)
     *    - 상품 정보 저장 또는 삭제 시 상품 카테고리에 영향 주는 부분이 없음 (이유)
     * */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    private ProductCategory productCategory;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, mappedBy = "product")
    private ProductReview productReview;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, mappedBy = "product")
    private List<ProductLike> likeProducts;

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
    @Column(insertable = false)
    private LocalDateTime updateDatetime;

    // 상품 삭제 요청 시 상품 상태를 1로 변경 (1 : 상품 삭제)
    public void updateProductStatus(int productStatus) {
        this.productStatus = productStatus;
    }

    public void updateProductTitle(String title) {
        this.title = title;
    }

    public void updateProductPrice(String price) {
        this.price = price;
    }

    public void updateProductInfo(String information) {
        this.information = information;
    }

    public void updateProductImgDir(String imgDirectory) {
        this.imgDirectory = imgDirectory;
    }

    public void updateProductIsNego(int isNego) {
        this.isNego = isNego;
    }

    public void updateProductTranactionMode(int transactionModeId) {
        this.transactionModeId = transactionModeId;
    }

    public void updateProductTransactionStatus(int transactionStatusId) {
        this.transactionStatusId = transactionStatusId;
    }

    public void updateProductCategory(ProductCategory productCategory) {
        this.productCategory = productCategory;
    }





}
