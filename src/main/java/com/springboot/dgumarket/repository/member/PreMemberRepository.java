package com.springboot.dgumarket.repository.member;

import com.springboot.dgumarket.model.member.PreMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PreMemberRepository extends JpaRepository<PreMember, Integer> {

    @Query("select pm from PreMember pm Where pm.webMail = :webMail And pm.status = 0")
    PreMember findByWebMail(String webMail);



}
