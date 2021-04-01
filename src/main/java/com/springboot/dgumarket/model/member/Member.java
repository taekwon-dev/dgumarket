package com.springboot.dgumarket.model.member;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.springboot.dgumarket.model.Role;
import com.springboot.dgumarket.model.chat.ChatRoom;
import com.springboot.dgumarket.model.product.Product;
import com.springboot.dgumarket.model.product.ProductCategory;
import lombok.*;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Set;
//

/**
 * Created by TK YOUN (2020-10-20 오전 8:16)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */
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

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinTable(
            name = "member_roles",
            joinColumns = { @JoinColumn(name = "member_id")},
            inverseJoinColumns = {@JoinColumn(name = "role_id")})
    @JsonIgnore
    private Set<Role> roles;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinTable(
            name = "member_categories",
            joinColumns = { @JoinColumn(name = "member_id")},
            inverseJoinColumns = {@JoinColumn(name = "category_id")})
    @JsonIgnore
    private Set<ProductCategory> productCategories;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<Product> products;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinTable(
            name = "user_block",
            joinColumns = { @JoinColumn(name = "user_id")},
            inverseJoinColumns = {@JoinColumn(name = "blocked_user_id")})
    @JsonIgnore
    private Set<Member> blockUsers; // 내가 차단한 유저들

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinTable(
            name = "user_block",
            joinColumns = { @JoinColumn(name = "blocked_user_id")},
            inverseJoinColumns = {@JoinColumn(name = "user_id")})
    @JsonIgnore
    private Set<Member> UserBlockedMe; // 나를 차단한 유저들 ( 나를 차단한 유저들의 물건들은 보여서는 안된다 )

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinTable(
            name = "product_like",
            joinColumns = { @JoinColumn(name = "user_id")},
            inverseJoinColumns = {@JoinColumn(name = "product_id")})
    @JsonIgnore
    private Set<Product> likeProducts; // 좋아요한 물건들

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
    public void like(Product product){
        this.getLikeProducts().add(product); // 좋아요 물건 추가
    }

    // 좋아요 취소하기
    public void unlike(Product product){
        this.getLikeProducts().remove(product); // 좋아요 했던 물건 취소하기
    }

    // 유저 차단하기
    public void blockUser(Member blockUser){
        this.getBlockUsers().add(blockUser);
    }

    // 유저 차단해제하기
    public void unblockUser(Member unblockUser){
        this.getBlockUsers().remove(unblockUser);
    }

    // 상대방에게 차단되었는 지 체크
    public boolean checkBlockedBy(Member targetUser){
        return this.getUserBlockedMe().contains(targetUser);
    }

    // -------------------------------------- 유저의 패널티 ---------------------------------------------------

    // 유저 경고유무
    public boolean checkWarnActive(){
        if(this.getAlertNum() >= 3){ // 경고횟수가 3회 이상
            LocalDateTime today = LocalDateTime.now();
            if(this.getAlertDate() != null){ // 경고받은일이 있고
                Period period = Period.between(this.getAlertDate().toLocalDate(), today.toLocalDate());
                if(period.getDays() <= 7){ // 패널티기간 지나지 않았다면
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




    public int checkBlockStatus(Member targetUser){
        if(this.getBlockUsers().contains(targetUser)){
            return 1; // 내가 상대방을 차단한 경우 ( 우선 )
        }else if(this.getUserBlockedMe().contains(targetUser)){
            return 2; // 남이 나를 차단한 경우
        }
        return 3; // 아무도 차단하지 않은 경우
    }

    public boolean IsBlock(Member targetUser){
        return this.getBlockUsers().contains(targetUser);
    }
}