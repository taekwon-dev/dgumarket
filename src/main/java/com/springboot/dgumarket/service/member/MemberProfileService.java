package com.springboot.dgumarket.service.member;

import com.springboot.dgumarket.dto.member.MemberInfoDto;
import com.springboot.dgumarket.dto.member.MemberUpdateDto;
import com.springboot.dgumarket.dto.member.SignUpDto;
import org.springframework.web.multipart.MultipartFile;


/**
 * Created by TK YOUN (2020-10-20 오전 8:45)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */
public interface MemberProfileService {

    // 여기는 최종적으로 회원 정보 관련 서비스 로직만 포함되도록 조정 예정 (2021-02-27)

    // 회원가입 3단계 - 회원가입 완료 시 호출
    void doSignUp(SignUpDto signUpDto);

    // 회원 탈퇴
    // userId : 탈퇴 요청을 한 유저의 고유 아이디 값
    boolean doWithdraw(int userId);

    boolean doCheckWebMail(String webMail);

    // 회원정보 조회 - 프로필 사진 디렉토리, 닉네임, 관심 카테고리s
    MemberInfoDto fetchMemberInfo(int userId);

    // 회원정보 수정 - 프로필 사진 디렉토리, 닉네임, 관심 카테고리s
    void updateMemberInfo(int userId, MemberUpdateDto memberUpdateInfoDto);

    // 회원 프로필 사진 DELETE
    boolean uploadProfileImgtoS3(MultipartFile multipartFile, String uploadName) throws Exception;

    // 회원 프로필 사진 DELETE
    boolean deleteProfileImgInS3(String deleteName);



}