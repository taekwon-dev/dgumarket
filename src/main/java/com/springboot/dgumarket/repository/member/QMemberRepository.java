package com.springboot.dgumarket.repository.member;

import com.springboot.dgumarket.dto.member.FindPwdDto;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QMemberRepository {

    // 비밀번호 찾기 - 핸드폰 인증문자 발송 API 중, 요청한 웹메일 핸드폰 번호 유효성 체크
    FindPwdDto findByWebMailForFindPwd(String webMail);

}
