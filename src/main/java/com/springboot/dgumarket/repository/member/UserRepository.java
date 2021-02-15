package com.springboot.dgumarket.repository.member;

import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.model.member.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by TK YOUN (2021-02-13 오후 8:34)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */
public interface UserRepository extends JpaRepository<User, Integer> {



}