package com.springboot.dgumarket.service.sms;


import com.springboot.dgumarket.dto.member.VerifyPhoneDto;
import com.springboot.dgumarket.exception.AligoSendException;
import com.springboot.dgumarket.exception.JsonParseFailedException;
import com.springboot.dgumarket.model.member.PreMember;
import com.springboot.dgumarket.repository.member.PreMemberRepository;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.apache.commons.lang3.RandomStringUtils;


@Service
public class SMSServiceImpl implements SMSService {

    // 예외 테스트
    // 문자 전송 관련 에러가 났을 때 롤백 처리 (https://www.netsurfingzone.com/spring/transactional-rollbackfor-example-using-spring-boot/)
    // 1일 5회 인증문자 발송요청 제한


    @Autowired
    private PreMemberRepository preMemberRepository;


    @Transactional
    @Override
    public void doSendSMSForPhone(VerifyPhoneDto verifyPhoneDto) {

        // 알리고 문자 API 서버 문자 전송 API 엔드포인트
        String BASE_URL = "https://apis.aligo.in/send/";

        // init
        int resultValue = 0;

        // 수신자 웹메일 (회원가입 절차 중)
        String webMail = verifyPhoneDto.getWebMail();

        // 수신자 핸드폰 번호
        String phoneNumber = verifyPhoneDto.getPhoneNumber();

        // 인증문자 6자리 랜덤 생성
        String verificationNumber = genVerifyNumForPhone();

        // 웹메일을 기반으로, 회원가입 절차에 있는 PreMember 관리
        // 웹메일에 해당하는 로우에 핸드폰 번호, 인증번호 값 업데이트
        PreMember preMember = preMemberRepository.findByWebMail(webMail);

        if (preMember == null) throw new AligoSendException("회원가입 유저 식별 에러");

        preMember.updatePhoneNumber(phoneNumber);
        preMember.updatePhoneVerificationNumber(verificationNumber);


        // 문자 본문 내용 (인증문자 포함)
        String text = "[인증번호 : " + verificationNumber + "] - 동대방네 (타인노출금지)";


        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("key", "god7s23jii5u2whi3ymlezr9jinfzxao"); // application.yml 값으로 재설정
        params.add("user_id", "dgumarket");                    // application.yml 값으로 재설정
        params.add("sender", "01022292983");                   // application.yml 값으로 재설정
        params.add("receiver", phoneNumber);
        params.add("msg", text);
//        params.add("testmode_yn", "Y");


        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(BASE_URL, params, String.class);

        // 문자 응답 값 파싱 예외
        try {

            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(responseEntity.getBody());

            resultValue = Integer.parseInt(jsonObject.get("result_code").toString());


            // Rollback 처리
            if (resultValue != 1) {
                // {
                //    "statusCode": 500,
                //    "timestamp": "2021-03-16T11:17:05.878+00:00",
                //    "message": "알리고 문자 전송 실패 : API 인증오류입니다.",
                //    "description": "uri=/api/send-sms/verify-phone"
                // }

                // 인증문자 발송이 실패했습니다. 다시 한 번 시도하시고 계속 문제가 있는 경우 관리자에게 문의해주세요. (클라이언트 측 안내)
                throw new AligoSendException("알리고 문자 전송 실패 : " + jsonObject.toString());
            }

        } catch (ParseException e) {
            // ParseException (Not RuntimeException = Checked Exception)
            // Unchecked Exception -> RuntimeException

            // {
            //    "statusCode": 500,
            //    "timestamp": "2021-03-16T11:17:05.878+00:00",
            //    "message": "알리고 문자 전송 API 응답 ParseException",
            //    "description": "uri=/api/send-sms/verify-phone"
            // }

            // 인증문자 발송이 실패했습니다. 다시 한 번 시도하시고 계속 문제가 있는 경우 관리자에게 문의해주세요. (클라이언트 측 안내)
            throw new JsonParseFailedException("알리고 문자 전송 API 응답 ParseException");
        }
    }

    // 6자리 난수 생성 (숫자)
    public String genVerifyNumForPhone() {
        return RandomStringUtils.randomNumeric(6);
    }
}
