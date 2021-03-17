package com.springboot.dgumarket.service.mail;

import com.springboot.dgumarket.model.member.PreMember;
import com.springboot.dgumarket.repository.member.PreMemberRepository;
import com.springboot.dgumarket.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;

/**
 * Created by TK YOUN (2020-11-09 오전 8:52)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */

// 웹메일 전송 버튼 클릭 시점에는 입력된 웹메일의 중복체크는 통과한 상황이다.
// 웹메일 전송 시점에는 해당 웹메일에 대응되는 고유값을 생성한 뒤 DB에 해당 값을 저장한다.
// 해당 고유값은 회원가입 2단계 (핸드폰 번호 인증) 링크에 첨부되어 유저가 해당 링크를 클릭해서 해당 페이지로 접근할 수 있도록 유도한다.

// [예외처리] - Rollback
// 메일 전송 과정에서 예외가 발생한 경우, DB에 저장된 고유 값을 롤백한다.
// MailException


@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private PreMemberRepository preMemberRepository;

    // Rollback : 메일 전송 과정에서 예외가 발생한 경우 데이터 변경에 대해서 롤백 처리가 필요하다.
    @Transactional
    @Override
    public void send(String receiverWebMail) {

        // 요청한 웹메일에 대응되는 고유값 생성 (고유값 유효기간 7일)
        String webMailJwt = jwtUtils.generateToken(receiverWebMail);


        PreMember preMember = preMemberRepository.findByWebMail(receiverWebMail);

        // 최초 '웹메일 전송 API' 요청 시점 (= 'preMemberRepository' 해당 웹메일 로우가 없는 경우)
        if (preMember == null) {
            preMember = PreMember.builder()
                    .webMail(receiverWebMail)
                    .webmailJwt(webMailJwt)
                    .build();

            // pre_members 테이블 값 삽입
            preMemberRepository.save(preMember);

        } else {
            // 해당 웹메일로 '웹메일 전송 API' 요청 내역이 있는 경우

            // 기존 정보 중 -> 핸드폰 번호, 핸드폰 인증 번호 초기화 (null)
            preMember.initPhoneNumber();
            preMember.initPhoneVerificationNumber();

            // 기존 정보 중 -> 웹메일, 웹메일 대응 고유값 -> 값 변경
            preMember.updateWebMailJwt(webMailJwt);

        }

        // HTML 태크를 활용하여 전송 메일의 본문을 구성 (진행 예정)
        // '여기'를 클릭하여 회원가입을 계속 진행해주세요. (웹메일 인증이 완료됐습니다)
        SimpleMailMessage message = new SimpleMailMessage();

        // 발신자 메일 :
        message.setFrom("donggukmk@gmail.com");

        // 수신자 메일 주소 :
        message.setTo(receiverWebMail);

        // 발신 메일 제목 :
        message.setSubject("제목");

        // 발신 메일 본문 :
//        message.setText("https://dgumarket.co.kr/shop/account/smartPhone_certification?user_id="+webMailJwt);
        message.setText("http://localhost:8081/shop/account/smartPhone_certification?user_id="+webMailJwt);


        // 웹메일 전송
        emailSender.send(message);


    }
}

