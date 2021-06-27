package com.springboot.dgumarket.controller.member;

import com.springboot.dgumarket.dto.member.ChangePhoneDto;
import com.springboot.dgumarket.dto.member.ChangePwdDto;
import com.springboot.dgumarket.dto.member.MemberInfoDto;
import com.springboot.dgumarket.dto.member.MemberUpdateDto;
import com.springboot.dgumarket.model.member.redis.RedisJwtToken;
import com.springboot.dgumarket.payload.response.ApiResultEntity;
import com.springboot.dgumarket.repository.member.redis.RedisJwtTokenRepository;
import com.springboot.dgumarket.service.UserDetailsImpl;
import com.springboot.dgumarket.service.member.MemberProfileService;
import com.springboot.dgumarket.service.sms.SMSService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.UUID;

/**
 * Created by TK YOUN (2021-01-01 오후 12:11)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */

@Slf4j
@RestController
@RequestMapping("/api/user/profile")
public class MemberProfileController {

    @Autowired
    private MemberProfileService memberService;

    @Autowired
    private SMSService smsService;

    @Autowired
    private RedisJwtTokenRepository redisJwtTokenRepository;

    // [회원정보 불러오기 : 프로필 사진, 닉네임, 관심 카테고리]
    @GetMapping("/read")
    public ResponseEntity<ApiResultEntity> getMemberInfo(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        MemberInfoDto memberInfoDto = memberService.fetchMemberInfo(userDetails.getId());

        ApiResultEntity apiResponseEntity = ApiResultEntity.builder()
                .message("회원 정보 조회")
                .responseData(memberInfoDto)
                .statusCode(200)
                .build();
        return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
    }

    // 회원 정보 (프로필 사진, 닉네임, 관심카테고리) 수정
    @PostMapping("/update")
    public ResponseEntity<ApiResultEntity> updateInfo(@Valid @RequestBody MemberUpdateDto memberUpdateInfoDto, Authentication authentication) {

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        memberService.updateMemberInfo(userDetails.getId(), memberUpdateInfoDto);

        ApiResultEntity apiResponseEntity = ApiResultEntity.builder()
                .message("회원 정보 수정")
                .responseData(null)
                .statusCode(200)
                .build();

        return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
    }



    // 회원 비밀번호 변경
    @PostMapping("/change-pwd")
    public ResponseEntity<ApiResultEntity> updatePwd(@RequestBody ChangePwdDto changePwdDto, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        ApiResultEntity apiResponseEntity = memberService.updatePassword(userDetails.getId(), changePwdDto);
        return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
    }

    // 회원 핸드폰번호 변경
    @PostMapping("/change-phone")
    public ResponseEntity<ApiResultEntity> updatePhone(@RequestBody ChangePhoneDto changePhoneDto, Authentication authentication) {

        // Authentication 객체가 주입되는 과정에서 예외가 발생하는 경우, 인터셉터에서 처리
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        ApiResultEntity apiResponseEntity = memberService.checkVerificationNunberForPhone(userDetails.getId(), changePhoneDto);

        return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
    }

    // 회원 핸드폰번호 변경 페이지에서 핸드폰 번호 확인 API
    @GetMapping("/get-phone")
    public ResponseEntity<ApiResultEntity> getPhone(Authentication authentication) {

        // Authentication 객체가 주입되는 과정에서 예외가 발생하는 경우, 인터셉터에서 처리
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        ApiResultEntity apiResponseEntity = memberService.getPhoneNumberForPhoneChange(userDetails.getId());

        return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
    }




    // 유저 프로필 이미지 업로드 (1장)
    // 예외처리
    // Unsupported Media Type
    @PostMapping("/image-upload")
    public ResponseEntity<ApiResultEntity> uploadProfileImg(Authentication authentication, @RequestParam("prevFileName") String prevFileName, @RequestParam("file") MultipartFile multipartFile) throws Exception {

        String fileName = null;
        String fileType = null;

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

        // 추가 작업 (2021-03-06)
        // 새로 업로드한 이미지 파일의 타입이 달라진 경우에 기존 파일명에 포함된 파일형식을 바꿔줘야 한다.
        if (fileName.equals("")) {
            fileName = UUID.randomUUID().toString().replace("-", "")+"."+fileType;
        }

        memberService.uploadProfileImgtoS3(multipartFile, fileName);

        ApiResultEntity apiResponseEntity = ApiResultEntity.builder()
                .message("유저 프로필 사진 업로드 성공")
                .responseData(fileName) // AWS S3 업로드된 파일명 반환
                .statusCode(200)
                .build();
        return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);

    }

    // @NotBlank not to accept {null, "", " "}
    // 삭제해야 하는 파일명 (원본과 리사이즈된 파일명이 모두 동일하므로) 값이 전제된 경우
    // 로직이 수행하도록 제한을 둔다.

    // 없는 경우 예외 처리는 어떻게? (클라이언트 측 / 서버 측)
    @PostMapping("/image-delete")
    public ResponseEntity<ApiResultEntity> deleteProfileImg(Authentication authentication, @RequestParam("deleteFileName") String deleteFileName) {
        // S3에 저장되어 있는 프로필 사진 삭제 로직 수행 서비스 호출
        memberService.deleteProfileImgInS3(deleteFileName);

        ApiResultEntity apiResponseEntity = ApiResultEntity.builder()
                .message("유저 프로필 사진 삭제 성공")
                .responseData(null) // AWS S3 업로드된 파일명 반환
                .statusCode(200)
                .build();
        return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<ApiResultEntity> doWithdraw(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {

        // UserDetails - 회원탈퇴 요청 유저 정보 -> User 고유 ID 추출 -> 서비스 레이어 파라미터로 전달.
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // 리프레시 토큰 값 init
        String refreshToken = null;


        Cookie[] cookies = request.getCookies();
        for (int i = 0; i < cookies.length; i++) {
            if (cookies[i].getName().equals("refreshToken")) {
                // HTTP 요청에 수반된 쿠키 값 중 'refreshToken' 키에 저장된 값
                refreshToken = cookies[i].getValue();
            }
        }

        // Service 로직 (탈퇴 처리 -> 회원 상태 값 변경)
        memberService.doWithdraw(userDetails.getId());

        // NPE 처리 해야 함
        RedisJwtToken redisJwtToken = redisJwtTokenRepository.findById(refreshToken).get();

        // Redis - R 토큰 삭제
        redisJwtTokenRepository.delete(redisJwtToken);

        // R 토큰 쿠키 삭제
        // create a cookie
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        // https 적용 상황에서 주석 풀고 테스트 하기.
        // cookie.setSecure(true);
        cookie.setMaxAge(0);

        // add cookie to response
        response.addCookie(cookie);

        ApiResultEntity apiResponseEntity = ApiResultEntity.builder()
                .message("유저 탈퇴 요청 처리 성공")
                .responseData(null)
                .statusCode(200)
                .build();
        return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);

    }
}
