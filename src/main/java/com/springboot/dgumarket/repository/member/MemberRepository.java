package com.springboot.dgumarket.repository.member;

import com.springboot.dgumarket.model.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Created by TK YOUN (2020-10-20 오전 8:18)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */

public interface MemberRepository extends JpaRepository<Member, Integer> {


    Optional<Member> findByWebMail(String webMail);

    // [회원 정보 조회] - 포로필 사진경로, 닉네임, 관심 카테고리
    Member findById(int id);

}
