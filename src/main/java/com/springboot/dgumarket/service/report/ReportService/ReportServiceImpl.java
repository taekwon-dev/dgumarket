package com.springboot.dgumarket.service.report.ReportService;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.springboot.dgumarket.exception.AligoSendException;
import com.springboot.dgumarket.exception.ErrorMessage;
import com.springboot.dgumarket.exception.JsonParseFailedException;
import com.springboot.dgumarket.model.chat.ChatRoom;
import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.model.product.Product;
import com.springboot.dgumarket.model.report.Report;
import com.springboot.dgumarket.model.report.ReportCategory;
import com.springboot.dgumarket.payload.request.report.ReportRequest;
import com.springboot.dgumarket.repository.chat.ChatRoomRepository;
import com.springboot.dgumarket.repository.member.MemberRepository;
import com.springboot.dgumarket.repository.product.ProductRepository;
import com.springboot.dgumarket.repository.report.ReportCategoryRepository;
import com.springboot.dgumarket.repository.report.ReportRepository;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Date;


@Service
public class ReportServiceImpl implements ReportService{

    // 알리고 문자 API 서버 문자 전송 API 엔드포인트
    private static String BASE_URL = "https://apis.aligo.in/send/";

    @Autowired
    ChatRoomRepository chatRoomRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ReportCategoryRepository reportCategoryRepository;

    @Autowired
    ReportRepository reportRepository;

    // 신고하기
    @Override
    public void postReport(int userId, ReportRequest reportRequest) {
        Member member = memberRepository.findById(userId);
        ReportCategory category = reportCategoryRepository.findById(reportRequest.getReport_category_id());

        Report report = Report.builder()
                .reporter(member)
                .reportEtcReason(reportRequest.getReport_etc_reason())
                .reportCategory(category).build();

        if(reportRequest.getReport_img_path().isPresent()){ // 업로드할 이미지가 있을 경우
            report.setReportImgDirectory(reportRequest.getReport_img_path().get());
        }

        // 개별물건페이지에서 요청온 경우
        if (reportRequest.getReport_product_id().isPresent()){
            Product targetProduct = productRepository.getOne(reportRequest.getReport_product_id().get());
            report.setTargetUser(targetProduct.getMember());
            report.setReportProduct(targetProduct);
        }

        // 채팅방에서 요청온 경우
        if (reportRequest.getReport_room_id().isPresent()){
            ChatRoom chatRoom = chatRoomRepository.getOne(reportRequest.getReport_room_id().get());
            report.setTargetUser(chatRoom.getMemberOpponent(member));
            report.setChatRoom(chatRoom);
            report.setReportProduct(chatRoom.getProduct());
        }
        // 관리자에게 신고접수여부 알려주면 좋을것같음. 이곳에 휴대전화로 메시지알림가도록 하기
        // 만약 메시지 전송과정에서 예외 발생하면 save 까지 가지 않기 떄문에 의도한 예외처리가 제대로 됨!
        String nickName = member.getNickName();
        sendSMS("\"" + nickName + "\"님의 신고가 정상적으로 접수됐습니다. 빠른 시일내에 처리하고 문자를 통해 안내해드리겠습니다.",
                member.getPhoneNumber());

        reportRepository.save(report); // 신고저장
    }

    /** 유저 신고 시 메시지 전송 예제 코드
     *
     * [Params]
     * @text : 메시지 전송 내용
     * @phoneNumber : 수신자 핸드폰 번호
     *
     * [Rollback 이슈]
     * 신고 관련 데이터를 데이터베이스에 저장하는 과정에서 예외가 발생하지 않은 경우 문자를 전송 처리를 한다.
     * 하지만, 문자를 전송하는 과정에서 예외가 발생하는 경우 데이터베이스에 입력된 데이터를 롤백 처리를 해야한다.
     * 따라서 postReport() 메소드에서 @Transactional 어노테이션을 붙여서 RuntimeException이 발생한 경우에
     * 롤백처리가 되도록 조치해야 한다.
     *
     * (참고) 롤백은 RuntimeException이 발생한 경우에만 적용된다.
     * 따라서 ParseException이 발생했을 때, 아래 AligoSendException (RuntimeException을 상속한 커스텀 예외 클래스)를
     * 통해 롤백처리를 유도한다.
     *
     * 이해 안 되는 경우, 따로 질문 바람 (로직 상 매우 중요한 포인트)
     *
     */
    private void sendSMS(String text, String phoneNumber) {


        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("key", "god7s23jii5u2whi3ymlezr9jinfzxao");         // 알리고 서버 인증 키 (추후 application.yml으로 설정한 뒤 가져올 예정)
        params.add("user_id", "dgumarket");                            // 알리고 서버에 등록된 유저 로그인 아이디 (추후 application.yml으로 설정한 뒤 가져올 예정)
        params.add("sender", "01022292983");                           // 발신자 번호 (추후 application.yml으로 설정한 뒤 가져올 예정)
        params.add("receiver", phoneNumber);                              // 수신자 번호
        params.add("msg", text);                                          // 메시지 본문

        // testmode_yn 을 Y로 설정하는 경우 과금프로세스와 실제문자 전송을 제외한 나머지가 실제와 동일하게 동작이 되므로 연동작업시 유용하게 사용하실 수 있다.
//        params.add("testmode_yn", "Y");


        // RestTemplate을 통해 알리고 서버에 요청
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(BASE_URL, params, String.class);

        // 알리고 서버로부터 응답 받은 오브젝트 파싱하는 과정에서 ParseException 발생할 수 있으므로,
        // try-catch 문으로 아래와 같이 코드 진행
        // catch 문에서 잡히는 ParseException가 발생하는 경우, -100 코드를 통해 유저에게 "다시 한 번 시도하시고 계속 문제가 있는 경우 관리자에게 문의해주세요."
        // 안내를 한다 (-> 예외 상황으로, 우리가 작성한 서비스 로직에서 발생하는 문제가 아니므로 위와 같이 처리)
        try {

            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(responseEntity.getBody());

            // 결과 메세지( result_code 가 0 보다 작은경우 실패사유 표기)
            // https://smartsms.aligo.in/admin/api/spec.html
            int resultValue = Integer.parseInt(jsonObject.get("result_code").toString());


            // Rollback
            if (resultValue < 0) {

                // [Exception]
                // {errorCode < 0 : 인증문자 발송이 실패했습니다. 다시 한 번 시도하시고 계속 문제가 있는 경우 관리자에게 문의해주세요. (클라이언트 측 안내)}
                throw new AligoSendException(errorResponse("알리고 문자 전송 실패, 실패 사유 : " + jsonObject.get("message").toString(), resultValue, "/api/send-sms/change-phone"));
            }

        } catch (ParseException e) {
            // ParseException (Not RuntimeException = Checked Exception) 이므로, 예외 발생 시 롤백 처리 대상이 아님
            // Unchecked Exception -> RuntimeException 따라서 ParseException 발생 시 런타임 예외를 던짐으로써 롤백처리를 유도함

            // [Exception]
            // {errorCode < 0 : 인증문자 발송이 실패했습니다. 다시 한 번 시도하시고 계속 문제가 있는 경우 관리자에게 문의해주세요. (클라이언트 측 안내)}
            throw new JsonParseFailedException(errorResponse("알리고 문자 전송 API 응답 ParseException", -100, "/api/send-sms/change-phone"));
        }
    }

    // 문자 전송 API 관련 에러 응답 오브젝트 생성
    private String errorResponse(String errMsg, int errorCode, String requestPath) {

        // [ErrorMessage]
        // {
        //     int statusCode : 응답 코드
        //     Date timestamp : 요청 시간
        //     String message : 메시지
        //     String requestPath : 요청 경로
        //     String pathToMove : (조건부) 클라이언트 측에서 이동할 페이지 경로 (statusCode 따라서 예외 결과 클라이언트가 '특정' 페이지로 요청해야 하는 경우가 있다.)
        // }


        // init
        ErrorMessage errorMessage = null;
        // 최종 클라이언트에 반환 될 예외 메시지 (JsonObject as String)
        String errorResponse = null;



        // errorCode 0 보다 작은 경우 (알리고 문자 전송 API), 에러 사유 표시 - message
        if (errorCode < 0) {
            errorMessage = ErrorMessage
                    .builder()
                    .statusCode(errorCode)
                    .timestamp(new Date())
                    .message(errMsg)
                    .requestPath(requestPath)
                    .build();
        } else {
            // 그 외, 로직 상 특정 페이지로 요청하도록 유도해야 하는 경우
            errorMessage = ErrorMessage
                    .builder()
                    .statusCode(errorCode)
                    .timestamp(new Date())
                    .message(errMsg)
                    .requestPath(requestPath)
                    .pathToMove("/")
                    .build();

        }


        Gson gson = new GsonBuilder().create();

        errorResponse = gson.toJson(errorMessage);

        return errorResponse;
    }
}
