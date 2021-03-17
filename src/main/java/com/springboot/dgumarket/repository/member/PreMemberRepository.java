package com.springboot.dgumarket.repository.member;

import com.springboot.dgumarket.model.member.PreMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PreMemberRepository extends JpaRepository<PreMember, Integer> {

    PreMember findByWebMail(String webMail);



}
