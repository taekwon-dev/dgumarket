package com.springboot.dgumarket.exception;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.jsonwebtoken.JwtException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;


@RestControllerAdvice
public class ControllerExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ControllerExceptionHandler.class);


    @ExceptionHandler(JwtException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public ErrorMessage resourceNotFoundException(JwtException ex, WebRequest request) {

        ErrorMessage message = new ErrorMessage(
                HttpStatus.NOT_FOUND.value(),
                new Date(),
                ex.getMessage(),
                request.getDescription(false));

        return message;
    }

    // RuntimeException
    @ExceptionHandler({RuntimeException.class, IncorrectResultSizeDataAccessException.class})
//    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorMessage globalExceptionHandler(Exception ex) {

        // Exception 객체, getMessage()를 통해 해당 메시지에서
        // 각 에러 객체 요소의 값을 채운다.
        // example -
        // {"errorCode":-1,"timestamp":"Mar 19, 2021 6:30:35 PM","message":"이미 회원가입한 유저가 회원가입 API 요청한 경우","requestPath":"/api/user/signup","pathToMove":"/shop/main/index"}
        // pathToMove 요소는 nullable

        int resultCode = -100;
        String errorMessage = null;
        String requestPath = null;
        String pathToMove = null;

        ErrorMessage errorObject = null;

        try {
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(ex.getMessage());

            resultCode = Integer.parseInt(jsonObject.get("resultCode").toString());
            errorMessage = jsonObject.get("message").toString();
            requestPath = jsonObject.get("requestPath").toString();

            // resultCode에 따라서 pathToMove 값이 결정된다.
            // {resultCode 300 ~ 399 -> pathToMove 페이지 주소 값이 들어온다}
            if (resultCode >= 300 && resultCode < 320) pathToMove = jsonObject.get("pathToMove").toString();

            if (pathToMove != null) {
                errorObject = ErrorMessage
                        .builder()
                        .resultCode(resultCode)
                        .timestamp(new Date())
                        .message(errorMessage)
                        .requestPath(requestPath)
                        .pathToMove(pathToMove)
                        .build();
            } else {
                errorObject = ErrorMessage
                        .builder()
                        .resultCode(resultCode)
                        .timestamp(new Date())
                        .message(errorMessage)
                        .requestPath(requestPath)
                        .build();
            }
            return errorObject;

        } catch (ParseException e) {
            throw new JsonParseFailedException(errorResponse("RuntimeException 에러 메시지 파싱 과정에서 예외 발생(ControllerExceptionHandler)", 500, ""));
        }
    }

    @ExceptionHandler(MailException.class)
    public ResponseEntity<?> mailExceptionHandler(MailException ex, WebRequest request) {
        ErrorMessage message = new ErrorMessage(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                new Date(),
                ex.getMessage(),
                request.getDescription(false));

        return new ResponseEntity<>(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @ExceptionHandler(CustomControllerExecption.class)
    public ResponseEntity<?> exceptionHandler(CustomControllerExecption ex, WebRequest request) {
        ErrorMessage message = new ErrorMessage(
                ex.getHttpStatus().value(),
                new Date(),
                ex.getMessage(),
                request.getDescription(false));

        return new ResponseEntity<>(message, ex.getHttpStatus());
    }


    // 유저 프로필 관련 API 예외 메시지 오브젝트 생성 및 리턴
    public String errorResponse(String errMsg, int resultCode, String requestPath) {

        // [ErrorMessage]
        // {
        //     int resultCode;
        //     Date timestamp;
        //     String message;
        //     String requestPath;
        //     String pathToMove;
        // }

        // init
        ErrorMessage errorMessage = null;

        // 최종 클라이언트에 반환 될 예외 메시지 (JsonObject as String)
        String errorResponse = null;

        errorMessage = ErrorMessage
                .builder()
                .resultCode(resultCode)
                .timestamp(new Date())
                .message(errMsg)
                .requestPath(requestPath)
                .build();

        Gson gson = new GsonBuilder().create();

        errorResponse = gson.toJson(errorMessage);

        return errorResponse;
    }




}

