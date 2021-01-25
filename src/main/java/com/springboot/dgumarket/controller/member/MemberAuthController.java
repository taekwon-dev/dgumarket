package com.springboot.dgumarket.controller.member;

import com.drew.imaging.ImageProcessingException;
import com.springboot.dgumarket.dto.member.MemberInfoDto;
import com.springboot.dgumarket.dto.member.MemberUpdateDto;
import com.springboot.dgumarket.payload.response.ApiResponseEntity;
import com.springboot.dgumarket.service.UserDetailsImpl;
import com.springboot.dgumarket.service.member.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;

/**
 * Created by TK YOUN (2021-01-01 오후 12:11)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */

@RestController
@RequestMapping("/user/auth")
public class MemberAuthController {

    @Autowired
    private MemberService memberService;

    // [회원정보 불러오기 : 프로필 사진, 닉네임, 관심 카테고리]
    @GetMapping("/profile")
    public ResponseEntity<ApiResponseEntity> getMemberInfo(Authentication authentication) {

        if (authentication != null) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            MemberInfoDto memberInfoDto = memberService.fetchMemberInfo(userDetails.getId());

            ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                    .message("회원 정보 조회")
                    .data(memberInfoDto)
                    .status(200)
                    .build();
            return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
        } else {

        }

        return null;
    }

    // 회원 정보 (프로필 사진, 닉네임, 관심카테고리) 수정
    @PostMapping("/profile-update")
    @Transactional
    public ResponseEntity<ApiResponseEntity> updateInfo(@Valid @RequestBody MemberUpdateDto memberUpdateInfoDto, Authentication authentication) {


        if (authentication != null) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            memberService.updateMemberInfo(userDetails.getId(), memberUpdateInfoDto);

            ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                    .message("회원 정보 수정")
                    .data(null)
                    .status(200)
                    .build();

            return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
        } else {

        }
        return null;
    }

    // [프로필 사진 업로드]
    @PostMapping("/profileimg-upload")
    public ResponseEntity<ApiResponseEntity> uploadProfileImage(@RequestParam("image") MultipartFile multipartFile, Authentication authentication)
            throws IOException, ImageProcessingException {

        if (authentication != null) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            String fileName = memberService.uploadImageAndResize(multipartFile, userDetails.getId());

            ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                    .message("회원 프로필 이미지 업로드")
                    .data(fileName)
                    .status(200)
                    .build();
            return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);

        } else {
            return null;
        }
    }
}
