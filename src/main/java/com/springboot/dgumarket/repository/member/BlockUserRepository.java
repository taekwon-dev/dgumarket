package com.springboot.dgumarket.repository.member;

import com.springboot.dgumarket.model.member.BlockUser;
import com.springboot.dgumarket.model.member.Member;

import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

public interface BlockUserRepository extends JpaRepository<BlockUser, Integer> {


    BlockUser findByUserAndBlockedUser(Member user, Member blockedUser);

    List<BlockUser> findByUserOrBlockedUser(Member user, Member user2);

}
