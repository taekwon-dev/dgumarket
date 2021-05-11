package com.springboot.dgumarket.model.member;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.springboot.dgumarket.model.Role;
import com.springboot.dgumarket.model.chat.ChatRoom;
import com.springboot.dgumarket.model.product.Product;
import com.springboot.dgumarket.model.product.ProductCategory;
import com.springboot.dgumarket.model.product.ProductLike;
import com.springboot.dgumarket.model.product.ProductReview;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Slf4j
@Entity
@Table(name = "members")
@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor
@DynamicUpdate
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // 유저 웹 메일
    @NotBlank
    private String webMail;

    // 유저 핸드폰 번호
    @NotBlank
    private String phoneNumber;

    // 유저 닉네임
    @NotBlank
    @Size(max = 20)
    private String nickName;

    // 비밀번호
    @NotBlank
    @Size(max = 60)
    private String password;

    // 프로필 이미지 경로
    private String profileImageDir;

    // 회원 - 이용제한 여부
    private int isEnabled;

    // 회원 - 탈퇴 여부
    private int isWithdrawn;

    // 회원 가입 일시
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createDatetime;

    // 회원 정보 마지막 수정 일시 (회원정보 수정 또는 회원 탈퇴)
    @UpdateTimestamp
    private LocalDateTime updateDatetime;

    // cascade 적용하지 않아도, 회원삭제 시 삭제됨을 확인
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "member_roles",
            joinColumns = { @JoinColumn(name = "member_id")},
            inverseJoinColumns = {@JoinColumn(name = "role_id")})
    @JsonIgnore
    private Set<Role> roles;

    // cascade 적용하지 않아도, 회원삭제 시 삭제됨을 확인
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "member_categories",
            joinColumns = { @JoinColumn(name = "member_id")},
            inverseJoinColumns = {@JoinColumn(name = "category_id")})
    @JsonIgnore
    private Set<ProductCategory> productCategories;

    /** [유저 : 상품]
     *  - 종속 관계 (= 실제 테이블에서 회원 테이블에 대한 조인 컬럼 보유)
     *  - Members : Product = One : Many
     *  - Fetch 옵션 : FetchType.LAZY (지연로딩 우선 적용 후 추후 경과보고 판단, @OneToMany 디폴트 로딩 옵션)
     *  - Cascade 옵션 : REMOVE
     *    - 회원 탈퇴 요청 시 30일 간 데이터베이스 보존 기간 이후 삭제되는 시점에 회원과 연관된 정보를 삭제처리 해야 함
     *      - 따라서 영속성 전이 옵션 중 REMOVE 적용
     *      - [PERSIST] 적용하지 않은 이유
     *        - 회원정보를 저장하는 시점에 상품 정보를 저장하는 것을 고려할 필요 없음
     *        - 단, 위에서 적용한 REMOVE 로직에 PERSIST가 반드시 필요하다면 추후 추가할 예정
     *      - [orphanRemoval]을 적용할 수 있는 이유 (아직 적용해야 하는 지 확신하지 않은 상태에서 기술)
     *        - 회원 & 상품의 관계가 끊어지는 시점은 사실상 회원 테이블에서 해당 회원 로우가 삭제되는 시점
     *        - 해당 상품의 정보 역시 삭제되는 것이 맞고, 이 때 해당 상품을 참고하는 다른 관계가 없으므로
     *        - 사실상 회원 & 상품의 관계는 회원이 해당 상품을 "개인 소유"하고 있다고 봐도 무방하다.
     * */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "member", cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    @JsonIgnore
    private Set<Product> products;

    // 로그인 유저가 차단한 유저 리스트 (-> 차단한 유저 리스트 조회하기)
    // 이 부분만 영속성 전이 (PERSIT) 추가해도 데이터베이스 INSERT (차단하기 메소드에서 적용)
    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, mappedBy = "user")
    @JsonIgnore
    private Set<BlockUser> blockUsers;


    // 로그인 유저를 차단한 유저 리스트 (-> 서비스 로직 중, 로그인 유저를 차단한 유저의 정보를 제외)
    // Cascade.REMOVE (= 삭제된 유저가 차단된 리스트에 있을 때 부여해야함)
    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, mappedBy = "blockedUser")
    @JsonIgnore
    private Set<BlockUser> UserBlockedMe;

    /** [유저-상품 : 좋아요 누른 상품]
     *  - 종속 관계 (mappedBy 속성, 외래 키가 product_like 테이블에서 관리 되고 있다)
     *  - cascade
     *      - PERSIST : ProductLike 객체를 새로 생성해서 추가할 때, 회원 정보를 조회하는데 이 때 PERSIST를 통해 미리 영속화 처리를 한다.
     *                  이를 통해 양방향 관계를 맺을 때 ProductLike 객체를 조회할 필요가 없다. (효율)
     *      - REMOVE : 회원 정보가 삭제될 때, 해당 회원이 좋아요 처리한 상품 역시 삭제된다.
     *
    */
    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, mappedBy = "member")
    private List<ProductLike> likeProducts;

    /** [유저(구매자) - 리뷰]
     *  - 양방향 관계 중, 종속 관계로 설정
     *      - 구매물품에 대한 리뷰 조회 시, product_review -> member 객체 탐색
     *      - member -> product_review 관계에서는 영속성 전이 관계를 위해 설정
     *  - 영속성 전이 : Cascade.REMOVE
     *      - 유저 정보가 삭제 됐을 경우, product_review에서 구매자 컬럼에 해당되는 정보를 삭제
     *      - product_review 테이블 (= 이미 구매한 상품임을 전제)
     *      - 판매자 입장에서 해당 상품에 대한 구매후기가 남겨진 경우, 해당 리뷰가 삭제처리
     *
     * */
    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.REMOVE}, mappedBy = "consumer")
    private List<ProductReview> reviewsConsumerProducts;

    /** [유저(판매자) - 리뷰]
     *  - 양방향 관계 중, 종속 관계로 설정
     *      - 구매물품에 대한 리뷰 조회 시, product_review -> member 객체 탐색
     *      - member -> product_review 관계에서는 영속성 전이 관계를 위해 설정
     *  - 영속성 전이 : Cascade.REMOVE
     *      - 유저 정보가 삭제 됐을 경우, product_review에서 판매자 컬럼에 해당되는 정보를 삭제
     *      - product_review 테이블 (= 이미 구매한 상품임을 전제)
     *      - 구매자 입장에서 구매 상품 조회할 때, 해당 상품이 리스트에서 제외되고 실제 리뷰 정보도 사라지게 된다.
     * */
    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.REMOVE}, mappedBy = "seller")
    private List<ProductReview> reviewsSellerProducts;


    @Column(name = "alert_num")
    private int alertNum; // 받은 경고 횟수

    @Column(name = "alert_date")
    private LocalDateTime alertDate; // 최신경고 받은 날짜

    @Column(name = "is_withdrawn_date")
    private LocalDateTime isWithdrawnDate; // 탈퇴 날짜

    @Column(name = "is_enabled_date")
    private LocalDateTime isEnabledDate; // 유저제재 받은 날짜

    /** ------------------------------------------------------------------------------------------------------------- */
    // [회원정보 수정 API] (이미지 저장 경로 값 변경 시 활용)
    // [회원탈퇴 API] - 탈퇴 요청 시 이미지 저장 경로 값 NULL
    public void updateProfileImgDir(String profileImgDir) {
        this.profileImageDir = profileImgDir;
    }

    // [회원정보 수정 API] (닉네임 변경)
    // [회원탈퇴 API] - 탈퇴 요청 시 닉네임 값 '알 수 없음'
    public void updateNickName(String nickName) {
        this.nickName = nickName;
    }

    // [회원정보 수정 API] 유저의 관심 카테고리 업데이트
    // 기존 관심 카테고리를 모두 삭제한다. (이 과정 없이, 2번 코드만 진행되도 실제로 delete query 호출)
    // 새로 입력 받은 관심 카테고리를 입력한다.
    public void updateCategories(Set<ProductCategory> productCategories) {
        this.productCategories.clear();
        this.productCategories = productCategories;
    }

    // [회원탈퇴 API]
    // 회원탈퇴 요청 시 회원 탈퇴 상태 값을 1로 수정한다. (일정 기간 보호 후 삭제)
    public void updateMemberStatus(int isWithdrawn) {
        this.isWithdrawn = isWithdrawn;
    }

    // [비밀번호 변경 API] - 비밀번호 변경 요청 시 비밀번호 값 업데이트
    public void updatePassword(String password) {
        this.password = password;
    }

    public void updatePhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    // [회원탈퇴시 모든 회원과 관련된 모든 채팅방 나가도록 하는 함수 3/5, by ms]
    /**
     * @param chatRooms 회원과 관련된 채팅방들
     */
    public void leave(List<ChatRoom> chatRooms){
       chatRooms.forEach(e -> e.leave(this.getId())); // userId 이용 -> 정확히 seller, consumer 판단하여 deleted = 1 넣어줌
    }

    // 좋아요 누르기
    public void like(ProductLike productLike){
        // 양방향 관계 (회원 : 좋아요한 상품)

        // 1. 멤버 <- 좋아요한 상품 리스트에 추가
        this.getLikeProducts().add(productLike);

        // 2. 좋아요한 상품 <- 멤버 매핑
        productLike.setMember(this);
    }

    // 좋아요 취소하기
    public void unlike(ProductLike productLike){
        // 양방향 관계 (회원 : 좋아요한 상품)

        // 1. 멤버 <- 좋아요한 상품 리스트에서 삭제
        this.getLikeProducts().remove(productLike);
    }

    // 유저 차단하기
    public void blockUser(BlockUser blockUser, Member blocker, Member blockedUser){

        // Member -> BlockUser
        this.getBlockUsers().add(blockUser);

        // BlockUser -> Member
        blockUser.setBlocker(blocker);
        blockUser.setBlockedUser(blockedUser);

    }

    // 유저 차단해제하기
    public void unblockUser(BlockUser unblockUser){

        // Member -> BlockUser
        this.getBlockUsers().remove(unblockUser);
    }


    // -------------------------------------- 유저의 패널티 ---------------------------------------------------
    // 유저 경고유무
    public boolean checkWarnActive(){
        if(this.getAlertNum() >= 3){ // 경고횟수가 3회 이상
            System.out.println("[체크] 경고가 3회 이상");
            LocalDateTime today = LocalDateTime.now();
            if(this.getAlertDate() != null){ // 경고받은일이 있고
                System.out.println("[체크] 경고받은일이 있고");
                Duration period = Duration.between(this.getAlertDate(), today);
                if(period.abs().toDays() <= 7){ // 패널티기간 지나지 않았다면
                    return true;
                }
            }
        }
        return false;
    }

    // 유저 경고추가
    public void addWarn(){
        this.alertNum += 1;
        this.setAlertDate(LocalDateTime.now());
    }

    // 유저 경고 취소
    public void cancelWarn(){
        this.alertNum -= 1;
        if(this.alertNum < 0){
            this.alertNum = 0;
        }
        this.setAlertDate(null);
    }

    // 유저 제재가하기
    public void punish(){
        this.setIsEnabled(1);
        this.setIsEnabledDate(LocalDateTime.now());
    }

    // 유저 제재 취소하기
    public void unPunish(){
        this.setIsEnabled(0);
    }

    public int checkBlockStatus(BlockUser blockUser, BlockUser blockedUser) {

        // this = 로그인 유저
        // Member targetUser = 타겟 유저 (차단 여부 검사)

        if (this.getBlockUsers().contains(blockUser)) {
            return 1; // 내가 상대방을 차단한 경우 ( 우선 )
        } else if (this.getUserBlockedMe().contains(blockedUser)) {
            return 2; // 남이 나를 차단한 경우
        }
        return 3; // 아무도 차단하지 않은 경우
    }

    public boolean IsBlock(BlockUser blockUser) {
        // 로그인 유저가 차단한 리스트에 채팅 상대방이 포함 여부 반환 (true or false)
        return this.getBlockUsers().contains(blockUser);
    }
}