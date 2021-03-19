package com.springboot.dgumarket.admin.repository;

import com.springboot.dgumarket.admin.model.AdminReportResult;
import com.springboot.dgumarket.model.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminReportResultRepository extends JpaRepository<AdminReportResult, Integer> {

    AdminReportResult findByMember(Member member); // 신고유저가 있는 지 확인
}
