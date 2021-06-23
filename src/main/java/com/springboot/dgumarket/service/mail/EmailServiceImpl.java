package com.springboot.dgumarket.service.mail;

import com.springboot.dgumarket.model.member.PreMember;
import com.springboot.dgumarket.repository.member.PreMemberRepository;
import com.springboot.dgumarket.utils.JwtUtils;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.transaction.Transactional;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

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
    private Configuration configuration;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private PreMemberRepository preMemberRepository;

    // Rollback : 메일 전송 과정에서 예외가 발생한 경우 데이터 변경에 대해서 롤백 처리가 필요하다.
    // MailException (extending RuntimeException) - Rollback 대상
    @Transactional
    @Override
    public void send(String receiverWebMail) throws MessagingException, MailException, IOException, TemplateException {

        // 요청한 웹메일에 대응되는 고유값 생성 (고유값 유효기간 7일)
        String webMailJwt = jwtUtils.genTokenForRegister2nd(receiverWebMail);


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


        MimeMessage message  = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());

        Template template = configuration.getTemplate("signup-email-template.ftl");

        Map<String, Object> model = new HashMap<>();
        model.put("Jwt", webMailJwt);

        String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);

        // From :
        helper.setFrom("donggukmk@gmail.com");

        // To
        helper.setTo(preMember.getWebMail());

        // Text
        helper.setText(html, true);

        helper.setSubject("[동대방네] 회원가입 이메일 인증 메일");


        // 웹메일 전송
        emailSender.send(message);
    }
}

