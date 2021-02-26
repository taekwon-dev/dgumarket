package com.springboot.dgumarket.service.member;

import com.drew.imaging.ImageProcessingException;
import com.springboot.dgumarket.dto.member.MemberInfoDto;
import com.springboot.dgumarket.dto.member.MemberUpdateDto;
import com.springboot.dgumarket.dto.member.SignUpDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Created by TK YOUN (2020-10-20 오전 8:45)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */
public interface MemberService {

    // 회원가입 3단계 - 회원가입 완료 시 호출
    SignUpDto doSignUp(SignUpDto signUpDto);

    boolean doCheckWebMail(String webMail);

    // 회원정보 조회 - 프로필 사진 디렉토리, 닉네임, 관심 카테고리s
    MemberInfoDto fetchMemberInfo(int user_id);

    // 회원정보 수정 - 프로필 사진 디렉토리, 닉네임, 관심 카테고리s
    void updateMemberInfo(int user_id, MemberUpdateDto memberUpdateInfoDto);

    // 회원 프로필 사진 DELETE
    boolean uploadProfileImgtoS3(MultipartFile multipartFile, String uploadName) throws Exception;

    // 회원 프로필 사진 DELETE
    boolean deleteProfileImgInS3(String deleteName);

    // 회원 탈퇴



}
