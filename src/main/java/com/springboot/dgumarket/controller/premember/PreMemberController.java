package com.springboot.dgumarket.controller.premember;

import com.springboot.dgumarket.dto.member.SignUpDto;
import com.springboot.dgumarket.dto.member.VerifyPhoneDto;
import com.springboot.dgumarket.payload.response.ApiResponseEntity;
import com.springboot.dgumarket.payload.response.ApiResultEntity;
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
    public ResponseEntity<ApiResultEntity> doCheckDupilcatePhone(@RequestBody VerifyPhoneDto verifyPhoneDto) {

        // init
        boolean resultFlag = false;

        // true : 중복체크 통과 (사용할 수 있는 경우), false : 중복체크 통과 실패 (사용할 수 없는 경우)
        resultFlag = preMemberService.doCheckDuplicatePhone(verifyPhoneDto);

        if (!resultFlag) {
            ApiResultEntity apiResponseEntity = ApiResultEntity.builder()
                    .resultCode(1)
                    .message("사용 할 수 없는 핸드폰 번호입니다.")
                    .responseData(null)
                    .build();

            return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);

        } else {
            ApiResultEntity apiResponseEntity = ApiResultEntity.builder()
                    .resultCode(2)
                    .message("사용 할 수 있는 핸드폰 번호입니다.")
                    .responseData(null)
                    .build();
            return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
        }
    }


    // [회원가입 2단계 - 핸드폰 인증 API]
    @PostMapping("/verify-phone")
    public ResponseEntity<ApiResultEntity> doVerifyPhone(@RequestBody VerifyPhoneDto verifyPhoneDto) {

        // init
        String token = null;

        // 핸드폰 인증번호 인증 결과 (통과 시 토큰 값, 통과하지 못한 경우 null 반환)
        token = preMemberService.doVerifyNumberForPhone(verifyPhoneDto);

        if (token == null) {

            ApiResultEntity apiResponseEntity = ApiResultEntity.builder()
                    .resultCode(1)
                    .message("핸드폰 인증 실패")
                    .responseData(null)
                    .build();


            return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
        } else {

            ApiResultEntity apiResponseEntity = ApiResultEntity.builder()
                    .resultCode(2)
                    .message("핸드폰 인증 성공")
                    .responseData(BASE_URL+token)
                    .build();

            return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
        }
    }


    // [회원가입 3단계 - 닉네임 중복체크 API]
    @PostMapping("/check-duplicate-nickname")
    public ResponseEntity<ApiResultEntity> doCheckDupilcateNickname(@RequestBody SignUpDto signUpDto) {

        // init
        boolean resultFlag = false;

        // true : 중복체크 통과 (사용할 수 있는 경우), false : 중복체크 통과 실패 (사용할 수 없는 경우)
        resultFlag = preMemberService.doCheckDupilicateNickname(signUpDto);

        if (!resultFlag) {

            ApiResultEntity apiResponseEntity = ApiResultEntity.builder()
                    .resultCode(1)
                    .message("입력 닉네임을 사용할 수 없습니다.")
                    .responseData(null)
                    .build();

            return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
        } else {
            ApiResultEntity apiResponseEntity = ApiResultEntity.builder()
                    .resultCode(2)
                    .message("입력 닉네임을 사용할 수 있습니다.")
                    .responseData(null)
                    .build();
            return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
        }
    }

}
