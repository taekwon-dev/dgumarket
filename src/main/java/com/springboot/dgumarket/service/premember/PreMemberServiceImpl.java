package com.springboot.dgumarket.service.premember;

import com.springboot.dgumarket.dto.member.SignUpDto;
import com.springboot.dgumarket.dto.member.VerifyPhoneDto;
import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.model.member.PreMember;
import com.springboot.dgumarket.repository.member.MemberRepository;
import com.springboot.dgumarket.repository.member.PreMemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PreMemberServiceImpl implements PreMemberService {

    @Autowired
    private PreMemberRepository preMemberRepository;

    @Autowired
    private MemberRepository memberRepository;

    // VerifyPhoneDto

    //{
    //    "webMail" : "example@dongguk.edu",
    //    "phoneNumber" : "01000000000", // - 없이 숫자만 입력
    //    "verificationNumber" : "6자리 숫자조합"
    //}

    @Override
    public boolean doCheckDuplicatePhone(VerifyPhoneDto verifyPhoneDto) {

        // 회원인 유저 중, 해당 핸드폰을 사용하는 유저가 있는 지 조회
        // isWithdrawn (0 : 회원, 1 : 탈퇴)
        Member member = memberRepository.findByPhoneNumberAndIsWithdrawnIs(verifyPhoneDto.getPhoneNumber(), 0);

        // 입력한 핸드폰 번호를 사용할 수 있는 경우
        if (member == null) return true;

        // 입력한 핸드폰 번호를 사용할 수 없는 경우
        return false;
    }

    @Override
    public String doVerifyNumberForPhone(VerifyPhoneDto verifyPhoneDto) {

        // init
        String comparisonValue = null;  // DB에 저장된 인증번호 값
        String inputValue = null;       // 유저가 입력한 인증번호 값


        // 인증문자 유효성 검사 (-> 웹메일, 핸드폰 번호 검증 식별 -> 인증문자 검사)
        PreMember preMember = preMemberRepository.findByWebMail(verifyPhoneDto.getWebMail());

        if (preMember != null) {
            comparisonValue = preMember.getPhoneVerificationNumber();

            // 입력한 웹메일로 조회되는 예비유저가 있지만,
            // 해당 로우에 인증번호 정보가 null 인 경우
            // [예외] - 불일치랑 동일한 응답
            if (comparisonValue == null) return null;

        } else {
            // 해당 웹메일로 조회할 수 있는 데이터가 없는 경우
            // [예외] - 불일치랑 동일한 응답
            return null;
        }


        inputValue = verifyPhoneDto.getVerificationNumber();


        // 인증번호 값 비교
        if (comparisonValue.equals(inputValue)) {
            // 인증번호 검사 결과 - 일치
            // 3단계 페이지로 이동 처리
            // return : 토큰 값 (3단계 페이지 요청 시 활용)
            return preMember.getWebmailJwt();

        } else {
            // 인증번호 검사 결과 - 불일치
            return null;
        }
    }

    // SignupDto

    // {
    //    "webMail" : "example@dongguk.edu",
    //    "phoneNumber" : "핸드폰번호",
    //    "nickName" : "닉네임",
    //    "password" : "비밀번호",
    //    "location" : "서울",
    //    "productCategories" : [1,2,3,4,5]
    // }

    // 닉네임 중복체크 API
    @Override
    public boolean doCheckDupilicateNickname(SignUpDto signUpDto) {

        // 유저가 입력한 닉네임 (-> 닉네임 중복체크 대상)
        String inputNickname = null;

        inputNickname = signUpDto.getNickName();

        // isWithdrawn {0 : 회원, 1 : 탈퇴회원}
        Member member = memberRepository.findByNickNameAndIsWithdrawnIs(inputNickname, 0);

        if (member == null) return true;

        return false;
    }
}
