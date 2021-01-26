package com.springboot.dgumarket.model.member;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.springboot.dgumarket.model.LoggedLogin;
import com.springboot.dgumarket.model.Role;
import com.springboot.dgumarket.model.product.Product;
import com.springboot.dgumarket.model.product.ProductCategory;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinTable(
            name = "member_roles",
            joinColumns = { @JoinColumn(name = "member_id")},
            inverseJoinColumns = {@JoinColumn(name = "role_id")})
    @JsonIgnore
    private Set<Role> roles;

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinTable(
            name = "member_categories",
            joinColumns = { @JoinColumn(name = "member_id")},
            inverseJoinColumns = {@JoinColumn(name = "category_id")})
    @JsonIgnore
    private Set<ProductCategory> productCategories;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<LoggedLogin> loggedLogins = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<Product> products;

    public Member(int id) {
        this.id = id;
    }

    public void addRoles(Role role) {
        this.roles.add(role);
        role.getMembers().add(this);
    }

    public void addProductCategories(ProductCategory productCategory) {
        this.productCategories.add(productCategory);
        productCategory.getMembers().add(this);
    }

    // 양방향 관계를 잘 생각해 A B
    public void addLoginLogging(LoggedLogin loggedLogin) {
        loggedLogin.setMember(this);
        this.getLoggedLogins().add(loggedLogin);
    }

    // 회원정보 수정 (임시 : 프로필 사진 디렉토리)
    public void updateProfileImgDir(String profileImgDir) {
        this.profileImageDir = profileImgDir;
    }

    // 회원정보 수정 (임시 : 닉네임)
    public void updateNickName(String nickName) {
        this.nickName = nickName;
    }

    // 유저의 관심 카테고리 업데이트
    // 1. 기존 관심 카테고리를 모두 삭제한다. (이 과정 없이, 2번 코드만 진행되도 실제로 delete query 호출)
    // 2. 새로 입력 받은 관심 카테고리를 입력한다.
    public void updateCategories(Set<ProductCategory> productCategories) {
        this.productCategories.clear();
        this.productCategories = productCategories;
    }


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