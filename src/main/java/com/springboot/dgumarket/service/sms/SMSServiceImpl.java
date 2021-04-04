package com.springboot.dgumarket.service.sms;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.springboot.dgumarket.dto.member.ChangePhoneDto;
import com.springboot.dgumarket.dto.member.VerifyPhoneDto;
import com.springboot.dgumarket.exception.AligoSendException;
import com.springboot.dgumarket.exception.ErrorMessage;
import com.springboot.dgumarket.exception.JsonParseFailedException;
import com.springboot.dgumarket.model.member.PhoneVerification;
import com.springboot.dgumarket.model.member.PreMember;
import com.springboot.dgumarket.payload.response.ApiResultEntity;
import com.springboot.dgumarket.repository.member.MemberRepository;
import com.springboot.dgumarket.repository.member.PhoneVerificationRepository;
import com.springboot.dgumarket.repository.member.PreMemberRepository;
import org.joda.time.DateTime;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;


@Service
public class SMSServiceImpl implements SMSService {

    // 알리고 문자 API 서버 문자 전송 API 엔드포인트
    private static String BASE_URL = "https://apis.aligo.in/send/";

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PreMemberRepository preMemberRepository;

    @Autowired
    private PhoneVerificationRepository phoneVerificationRepository;


    @Transactional
    @Override
    public ApiResultEntity doSendSMSForPhone(VerifyPhoneDto verifyPhoneDto) {

        // ResponseEntity
        ApiResultEntity apiResultEntity = null;

        // 핸드폰 인증 요청 횟수 (1일 5회 제한)
        // 초기화 1 (인증문자 발송 API 요청 시점)
        int count = 1;

        // 마지막 요청 날짜 (1일 5회 제한)
        LocalDateTime lastUpadateDateTime = null;

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
        if (preMember == null) throw new AligoSendException(errorResponse("회원 절차에 있는 예비 회원정보를 찾을 수 없는 경우", 301, "/api/send-sms/verify-phone"));

        // 인증문자 발송 횟수 (1일 기준)
        count = preMember.getCount();

        if (count < 5) {
            // 웹메일 인증 시 해당 로우가 이미 생성됐으므로, null인 핸드폰 번호, 인증번호 컬럼에 값 지정
            preMember.updatePhoneNumber(phoneNumber);
            preMember.updatePhoneVerificationNumber(verificationNumber);

            // 현재 count ++ 값
            count++;
            preMember.updateCount(count);
            // 인증문자 발송 시간 업데이트
            preMember.updateSmsSendDatetime(LocalDateTime.now());

            // 인증문자 발송
            sendSMS(verificationNumber, phoneNumber);
        } else {

            // 마지막 요청 DateTime
            lastUpadateDateTime = preMember.getSmsSendDatetime();

            // ex) 20210330
            String lastUpdateDate = lastUpadateDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String nowDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));


            if (Integer.parseInt(lastUpdateDate) < Integer.parseInt(nowDate)) {
                // 웹메일 인증 시 해당 로우가 이미 생성됐으므로, null인 핸드폰 번호, 인증번호 컬럼에 값 지정
                preMember.updatePhoneNumber(phoneNumber);
                preMember.updatePhoneVerificationNumber(verificationNumber);

                // count 1로 초기화
                preMember.initCount();
                // 인증문자 발송 시간 업데이트
                preMember.updateSmsSendDatetime(LocalDateTime.now());

                // 인증문자 발송
                sendSMS(verificationNumber, phoneNumber);

            } else {
                // 1일 5회 초과하는 경우
                // 문자 전송 실패
                apiResultEntity = ApiResultEntity
                        .builder()
                        .statusCode(1)
                        .message("1일 5회 이상 요청 제한으로 인증문자 발송 실패")
                        .responseData(null)
                        .build();

                return apiResultEntity;
            }
        }

        apiResultEntity = ApiResultEntity
                .builder()
                .statusCode(200)
                .message("인증문자가 발송됐습니다.")
                .responseData(null)
                .build();

        return apiResultEntity;



    }

    @Transactional
    @Override
    public ApiResultEntity doSendSMSForChangePhone(int userId, ChangePhoneDto changePhoneDto) {

        // ResponseEntity
        ApiResultEntity apiResultEntity = null;

        // 핸드폰 인증 요청 횟수 (1일 5회 제한)
        // 초기화 1 (인증문자 발송 API 요청 시점)
        int count = 1;

        // 마지막 요청 날짜 (1일 5회 제한)
        LocalDateTime lastUpadateTime = null;

        // 유저로 부터 입력 받은 핸드폰 번호
        String phoneNumber = changePhoneDto.getPhoneNumber();

        // 인증문자 발송 본문 - 인증문자 6자리 숫자 조합 값
        String phoneVerificationNumber = genVerifyNumForPhone();

        // 핸드폰 번호 요청한 유저의 기존 핸드폰 번호와 동일한 경우 예외처리 [필터]
        if (memberRepository.findByIdForChange(userId).getPhoneNumber().equals(phoneNumber)) {
            apiResultEntity = ApiResultEntity
                    .builder()
                    .statusCode(2)
                    .message("기존 핸드폰 번호와 동일한 번호로 인증문자를 발송할 수 없습니다.")
                    .responseData(null)
                    .build();

            return apiResultEntity;
        }


        // 핸드폰 번호 변경 관련 인증처리 위한 엔티티 객체
        // status {0 : 인 대기 1 : 인증 완료}
        PhoneVerification phoneVerification = phoneVerificationRepository.findByPhoneNumberAndStatusIs(phoneNumber, 0);

        // 최초
        if (phoneVerification == null) {

            phoneVerification = PhoneVerification
                    .builder()
                    .count(1)
                    .status(0)
                    .phoneNumber(phoneNumber)
                    .phoneVerificationNumber(phoneVerificationNumber)
                    .build();

            phoneVerificationRepository.save(phoneVerification);

            sendSMS(phoneVerificationNumber, phoneNumber);

            // 성공

        } else {
            // 유저의 인증 요청 횟수 (00일 기준)
            count = phoneVerification.getCount();
            // 마지막 요청 DateTime
            lastUpadateTime = phoneVerification.getUpdateDatetime();

            // ex) 20210330
            String lastUpdateDate = lastUpadateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String nowDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));


            if (count >= 5) {
                if (Integer.parseInt(lastUpdateDate) < Integer.parseInt(nowDate)) {

                    phoneVerification.updateCount(1); // 요청 횟수 1로 초기화
                    phoneVerification.updatePhoneVerificationNumber(phoneVerificationNumber);
                    phoneVerificationRepository.save(phoneVerification);

                    sendSMS(phoneVerificationNumber, phoneNumber);

                } else {
                    // 1일 5회 초과하는 경우
                    // 문자 전송 실패
                    apiResultEntity = ApiResultEntity
                            .builder()
                            .statusCode(1)
                            .message("1일 5회 이상 요청 제한으로 인증문자 발송 실패")
                           .responseData(null)
                            .build();

                    return apiResultEntity;
                }
            } else {

                count++; // 기존 요청 횟수 +1
                phoneVerification.updateCount(count);
                phoneVerification.updatePhoneVerificationNumber(phoneVerificationNumber);

                phoneVerificationRepository.save(phoneVerification);

                sendSMS(phoneVerificationNumber, phoneNumber);

            }
        }

        apiResultEntity = ApiResultEntity
                .builder()
                .statusCode(200)
                .message("인증문자 발송 성공 (핸드폰번호 변경)")
                .responseData(null)
                .build();

        return apiResultEntity;
    }

    // 6자리 난수 생성 (숫자)
    private String genVerifyNumForPhone() {
        return RandomStringUtils.randomNumeric(6);
    }


    // 문자전송 API 예외 메시지 오브젝트 생성 및 리턴
    private String errorResponse(String errMsg, int errorCode, String requestPath) {

        // [ErrorMessage]
        // {
        //     int statusCode;
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
                    .statusCode(errorCode)
                    .timestamp(new Date())
                    .message(errMsg)
                    .requestPath(requestPath)
                    .build();
        } else {
            // errorCode = 0
            // 메인 페이지로 이동 시키는 상황.
            errorMessage = ErrorMessage
                    .builder()
                    .statusCode(errorCode)
                    .timestamp(new Date())
                    .message(errMsg)
                    .requestPath(requestPath)
                    .pathToMove("/shop/main/index")
                    .build();

        }


        Gson gson = new GsonBuilder().create();

        errorResponse = gson.toJson(errorMessage);

        return errorResponse;
    }

    // 문자 전송
    private void sendSMS(String verificationNumber, String phoneNumber) {
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
            int resultValue = Integer.parseInt(jsonObject.get("result_code").toString());


            // Rollback 처리
            if (resultValue < 0) {

                // [Exception]
                // {errorCode < 0 : 인증문자 발송이 실패했습니다. 다시 한 번 시도하시고 계속 문제가 있는 경우 관리자에게 문의해주세요. (클라이언트 측 안내)}
                throw new AligoSendException(errorResponse("알리고 문자 전송 실패, 실패 사유 : " + jsonObject.get("message").toString(), resultValue, "/api/send-sms/change-phone"));
            }

        } catch (ParseException e) {
            // ParseException (Not RuntimeException = Checked Exception)
            // Unchecked Exception -> RuntimeException


            // [Exception]
            // {errorCode < 0 : 인증문자 발송이 실패했습니다. 다시 한 번 시도하시고 계속 문제가 있는 경우 관리자에게 문의해주세요. (클라이언트 측 안내)}
            throw new JsonParseFailedException(errorResponse("알리고 문자 전송 API 응답 ParseException", -100, "/api/send-sms/change-phone"));
        }
    }

}
