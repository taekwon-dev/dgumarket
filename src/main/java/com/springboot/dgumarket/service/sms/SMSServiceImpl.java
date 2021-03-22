package com.springboot.dgumarket.service.sms;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.springboot.dgumarket.dto.member.VerifyPhoneDto;
import com.springboot.dgumarket.exception.AligoSendException;
import com.springboot.dgumarket.exception.ErrorMessage;
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

import java.util.Date;


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

        // [Exception]
        // {errorCode : 0 -> 메인 페이지로 이동 시키기 위해 클라이언트에게 메인 페이지 경로 응답}
        if (preMember == null) throw new AligoSendException(errorResponse("회원 절차에 있는 예비 회원정보를 찾을 수 없는 경우", 301));

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

            // 결과 메세지( result_code 가 0 보다 작은경우 실패사유 표기)
            // https://smartsms.aligo.in/admin/api/spec.html
            resultValue = Integer.parseInt(jsonObject.get("result_code").toString());


            // Rollback 처리
            if (resultValue < 0) {

                // [Exception]
                // {errorCode < 0 : 인증문자 발송이 실패했습니다. 다시 한 번 시도하시고 계속 문제가 있는 경우 관리자에게 문의해주세요. (클라이언트 측 안내)}
                throw new AligoSendException(errorResponse("알리고 문자 전송 실패, 실패 사유 : " + jsonObject.get("message").toString(), resultValue));
            }

        } catch (ParseException e) {
            // ParseException (Not RuntimeException = Checked Exception)
            // Unchecked Exception -> RuntimeException


            // [Exception]
            // {errorCode < 0 : 인증문자 발송이 실패했습니다. 다시 한 번 시도하시고 계속 문제가 있는 경우 관리자에게 문의해주세요. (클라이언트 측 안내)}
            throw new JsonParseFailedException(errorResponse("알리고 문자 전송 API 응답 ParseException", -100));
        }
    }

    // 6자리 난수 생성 (숫자)
    public String genVerifyNumForPhone() {
        return RandomStringUtils.randomNumeric(6);
    }


    // 문자전송 API 예외 메시지 오브젝트 생성 및 리턴
    public String errorResponse(String errMsg, int errorCode) {

        // [ErrorMessage]
        // {
        //     int errorCode;
        //     Date timestamp;
        //     String message;
        //     String requestPath;
        //     String pathToMove;
        // }

        // errorCode에 따라서 예외 결과 클라이언트가 '특정' 페이지로 요청해야 하는 경우가 있다.
        // 그 경우 pathToMove 항목을 채운다.

        // init
        ErrorMessage errorMessage = null;

        // 최종 클라이언트에 반환 될 예외 메시지 (JsonObject as String)
        String errorResponse = null;

        // errorCode 0 보다 작은 경우 (알리고 문자 전송 API), 에러 사유 표시 - message
        // errorCode 0 인 경우 (인증문자 전송 대상 식별 불가한 상황)

        // 예외처리 결과 클라이언트가 __페이지를 요청해야 하는 경우
        // 해당 페이지 정보 포함해서 에러 메시지 반환
        if (errorCode < 0) {
            errorMessage = ErrorMessage
                    .builder()
                    .resultCode(errorCode)
                    .timestamp(new Date())
                    .message(errMsg)
                    .requestPath("/api/user/signup")
                    .build();
        } else {
            // errorCode = 0
            // 메인 페이지로 이동 시키는 상황.
            errorMessage = ErrorMessage
                    .builder()
                    .resultCode(errorCode)
                    .timestamp(new Date())
                    .message(errMsg)
                    .requestPath("/api/user/signup")
                    .pathToMove("/shop/main/index")
                    .build();

        }


        Gson gson = new GsonBuilder().create();

        errorResponse = gson.toJson(errorMessage);

        return errorResponse;
    }
}
