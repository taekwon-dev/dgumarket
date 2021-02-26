package com.springboot.dgumarket.controller.member;

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
import java.util.UUID;

/**
 * Created by TK YOUN (2021-01-01 오후 12:11)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */

@RestController
@RequestMapping("/api/user/profile")
public class MemberAuthController {

    @Autowired
    private MemberService memberService;

    // [회원정보 불러오기 : 프로필 사진, 닉네임, 관심 카테고리]
    @GetMapping("/read")
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
            // Exceptions (auth)
        }

        return null;
    }

    // 회원 정보 (프로필 사진, 닉네임, 관심카테고리) 수정
    @PostMapping("/update")
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

    // 유저 프로필 이미지 업로드 (1장)
    // 예외처리
    // Unsupported Media Type
    @PostMapping("/image-upload")
    public ResponseEntity<ApiResponseEntity> uploadProfileImg(Authentication authentication, @RequestParam("prevFileName") String prevFileName, @RequestParam("file") MultipartFile multipartFile) throws Exception {

        boolean result = false;
        String fileName = null;
        String fileType = null;

        if (authentication != null) {
            // 업로드 성공 시 반환되는 파일명 (이 값을 가지고 최종 회원 정보 업데이트)
            fileName = prevFileName;  // example.jpg or example.png ...
            fileType = multipartFile.getOriginalFilename().substring(multipartFile.getOriginalFilename().lastIndexOf(".") + 1);

            // 프로필 이미지 업로드 상황에서
            // S3에 등록된 파일명을 그대로 받아와서 업로드하는 경우
            // 예를 들어, A -> B로 프로필 사진을 변경하는 경우는
            // A 프로필 사진의 파일명을 (S3에 저장된) 그대로 받아서 다시 뒤집어 씌운다.

            // 반대로,
            // NULL -> A로, 기본 프로필 사진에서 A로 변경하는 경우는
            // 이미 기존 프로필 사진에 대한 파일명을 가지고 있지 않은 경우 (클라이언트 측)
            // 따라서, 새로운 고유한 파일명을 생성한다.
            if (fileName.equals("")) {
                fileName = UUID.randomUUID().toString().replace("-", "")+"."+fileType;
            }



            // result (boolean) 값에 업르도 결과 값 (true: 성공)
            result = memberService.uploadProfileImgtoS3(multipartFile, fileName);

            if (result) {
                ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                        .message("유저 프로필 사진 업로드 성공")
                        .data(fileName) // AWS S3 업로드된 파일명 반환
                        .status(200)
                        .build();
                return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
            } else {
                // 프로필 사진 업로드를 실패하는 경우 어떻게 처리할 지
                // 재요청 또는 ..
                ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                        .message("유저 프로필 사진 업로드 실패")
                        .data(null)
                        .status(200)
                        .build();
                return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
            }

        } else {
            // 게이트 웨이 인증 절차 통과 후
            // 다운스트림에서 인증실패하는 경우 예외처리
        }
        return null;
    }

    // @NotBlank not to accept {null, "", " "}
    // 삭제해야 하는 파일명 (원본과 리사이즈된 파일명이 모두 동일하므로) 값이 전제된 경우
    // 로직이 수행하도록 제한을 둔다.

    // 없는 경우 예외 처리는 어떻게? (클라이언트 측 / 서버 측)
    @PostMapping("/image-delete")
    public ResponseEntity<ApiResponseEntity> deleteProfileImg(Authentication authentication, @RequestParam("deleteFileName") String deleteFileName) {

        // init
        boolean result = false;

        if (authentication != null) {

            // S3에 저장되어 있는 프로필 사진 삭제 로직 수행 서비스 호출
            result = memberService.deleteProfileImgInS3(deleteFileName);

            if (result) {
                ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                        .message("유저 프로필 사진 삭제 성공")
                        .data(null) // AWS S3 업로드된 파일명 반환
                        .status(200)
                        .build();
                return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
            } else {
                // 프로필 사진 삭제를 실패하는 경우 어떻게 처리할 지
                // 재요청 또는 ..
                ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                        .message("유저 프로필 사진 삭제 실패")
                        .data(null)
                        .status(200)
                        .build();
                return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
            }

        } else {

        }
        return null;
    }
}
