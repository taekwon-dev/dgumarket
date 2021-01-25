package com.springboot.dgumarket.stomp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.springboot.dgumarket.exception.CustomJwtException;
import com.springboot.dgumarket.exception.ErrorMessage;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

import java.util.Date;

/**
 * Created by TK YOUN (2020-12-01 오후 10:13)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */

@Slf4j
@Component
public class CustomErrorHandler extends StompSubProtocolErrorHandler {

    private static final byte[] EMPTY_PAYLOAD = new byte[0];

    @Override
    public Message<byte[]> handleClientMessageProcessingError(Message<byte[]> clientMessage, Throwable ex) {
        Throwable exception = ex;

        log.error("ex instanceof CustomJwtException : {}", ex instanceof RuntimeException);
        log.error("excption.getMessage : {}", exception.getMessage());


        if (ex instanceof RuntimeException)
        {
            return handleUnauthorizedException(clientMessage, exception);
        }
        return super.handleClientMessageProcessingError(clientMessage, ex);
    }

    private Message<byte[]> handleUnauthorizedException(Message<byte[]> clientMessage, Throwable ex)
    {
        log.info("handleUnauthorizedException called");
        ErrorMessage errorMessage = new ErrorMessage(
                HttpStatus.UNAUTHORIZED.value(),
                new Date(),
                "JWT IS EXPIRED, PLEASE RELOGIN",
                "/shop/account/login");

        return prepareErrorMessage(clientMessage, errorMessage, "JWT Exception");

    }

    private Message<byte[]> prepareErrorMessage(Message<byte[]> clientMessage, ErrorMessage errorMessage, String errorCode)
    {
        log.info("prepareErrorMessage called");
        Gson gson = new GsonBuilder().create();
        String message = gson.toJson(errorMessage);
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.ERROR);

        accessor.setReceiptId(clientMessage.toString());
        accessor.setMessage(errorCode);
        accessor.setLeaveMutable(true);

        return MessageBuilder.createMessage(message != null ? message.getBytes() : EMPTY_PAYLOAD, accessor.getMessageHeaders());
    }
}
