package com.springboot.dgumarket.repository.member;

import com.springboot.dgumarket.model.member.FindPwd;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FindPwdVerificationRepository extends JpaRepository<FindPwd, Integer> {

    @Query("Select fp From FindPwd fp Where fp.webMail = :webMail And fp.phoneNumber = :phoneNumber And fp.status = 0")
    FindPwd findByWebMailAndPhoneNumber(String webMail, String phoneNumber);


}
