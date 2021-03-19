package com.springboot.dgumarket.repository.member;

import com.springboot.dgumarket.model.member.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by TK YOUN (2021-02-13 오후 8:34)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */
public interface UserRepository extends JpaRepository<User, Integer> {

    // [회원탈퇴 API] 회원탈퇴 요청 시 User 테이블에서 해당 유저 삭제
    User findByWebMail(String webMail);

    // 유저(member) 제재시 유저(user)에도 똑같이 제재적용하기
    User findById(int userId);
}