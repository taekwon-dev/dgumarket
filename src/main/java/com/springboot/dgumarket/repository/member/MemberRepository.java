package com.springboot.dgumarket.repository.member;

import com.springboot.dgumarket.model.member.BlockUser;
import com.springboot.dgumarket.model.member.Member;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by TK YOUN (2020-10-20 오전 8:18)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */

public interface MemberRepository extends JpaRepository<Member, Integer> {


    Optional<Member> findByWebMailAndIsWithdrawn(String webMail, int isWithdrawn);

    // [회원 정보 조회] - 포로필 사진경로, 닉네임, 관심 카테고리
    // 추후에 신고 제제 추가
    @Query("select m from Member m where m.isWithdrawn = 0 and m.id =:id")
    Member findById(int id);

    @Query("select b from BlockUser b where b.user.id=:userId and b.blockedUser.isWithdrawn=0 and b.blockedUser.isEnabled=0")
    List<BlockUser> findAllBlockUsers(int userId, @Nullable Pageable pageable);



}
