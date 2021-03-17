package com.springboot.dgumarket.controller.premember;

import com.springboot.dgumarket.dto.member.SignUpDto;
import com.springboot.dgumarket.dto.member.VerifyPhoneDto;
import com.springboot.dgumarket.payload.response.ApiResponseEntity;
import com.springboot.dgumarket.service.premember.PreMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pre-member/")
public class PreMemberController {

    private static final String BASE_URL = "/shop/account/userInfo_input?user_id=";

    @Autowired
    private PreMemberService preMemberService;

    // [회원가입 2단계 - 핸드폰 번호 중복체크 API]
    @PostMapping("/check-duplicate-phone")
    public ResponseEntity<ApiResponseEntity> doCheckDupilcatePhone(@RequestBody VerifyPhoneDto verifyPhoneDto) {

        // init
        boolean resultFlag = false;

        // true : 중복체크 통과 (사용할 수 있는 경우), false : 중복체크 통과 실패 (사용할 수 없는 경우)
        resultFlag = preMemberService.doCheckDuplicatePhone(verifyPhoneDto);

        if (!resultFlag) {
            ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                    .message("사용 할 수 없는 핸드폰 번호입니다.")
                    .data(null)
                    .status(1)
                    .build();
            return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);

        } else {
            ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                    .message("사용 할 수 있는 핸드폰 번호입니다.")
                    .data(null)
                    .status(2)
                    .build();
            return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
        }
    }


    // [회원가입 2단계 - 핸드폰 인증 API]
    @PostMapping("/verify-phone")
    public ResponseEntity<ApiResponseEntity> doVerifyPhone(@RequestBody VerifyPhoneDto verifyPhoneDto) {

        // init
        String token = null;

        // 핸드폰 인증번호 인증 결과 (통과 시 토큰 값, 통과하지 못한 경우 null 반환)
        token = preMemberService.doVerifyNumberForPhone(verifyPhoneDto);

        if (token == null) {
            ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                    .message("핸드폰 인증 실패")
                    .data(null)
                    .status(1)
                    .build();
            return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
        } else {
            ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                    .message("핸드폰 인증 성공")
                    .data(BASE_URL+token)
                    .status(2)
                    .build();
            return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
        }
    }


    // [회원가입 3단계 - 닉네임 중복체크 API]
    @PostMapping("/check-duplicate-nickname")
    public ResponseEntity<ApiResponseEntity> doCheckDupilcateNickname(@RequestBody SignUpDto signUpDto) {

        // init
        boolean resultFlag = false;

        // true : 중복체크 통과 (사용할 수 있는 경우), false : 중복체크 통과 실패 (사용할 수 없는 경우)
        resultFlag = preMemberService.doCheckDupilicateNickname(signUpDto);

        if (!resultFlag) {
            ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                    .message("입력 닉네임을 사용할 수 없습니다.")
                    .data(null)
                    .status(1)
                    .build();
            return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
        } else {
            ApiResponseEntity apiResponseEntity = ApiResponseEntity.builder()
                    .message("입력 닉네임을 사용할 수 있습니다.")
                    .data(null)
                    .status(2)
                    .build();
            return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
        }
    }

}
