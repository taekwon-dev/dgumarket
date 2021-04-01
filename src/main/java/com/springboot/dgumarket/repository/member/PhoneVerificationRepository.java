package com.springboot.dgumarket.repository.member;

import com.springboot.dgumarket.model.member.PhoneVerification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhoneVerificationRepository extends JpaRepository<PhoneVerification, Integer> {

    PhoneVerification findByPhoneNumberAndStatusIs(String phoneNumber, int status);

}
