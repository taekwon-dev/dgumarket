package com.springboot.dgumarket.controller.chat;

import com.springboot.dgumarket.dto.chat.ChatMessagesUnreadCountDto;
import com.springboot.dgumarket.payload.request.SendMessage;
import com.springboot.dgumarket.payload.response.ApiResponseEntity;
import com.springboot.dgumarket.service.UserDetailsImpl;
import com.springboot.dgumarket.service.chat.ChatMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;


@RestController
@RequestMapping("/chat")
public class ChatMessageController {
    private static final Logger logger = LoggerFactory.getLogger(ChatMessageController.class);

    @Autowired
    ChatMessageService chatMessageService;


    // [STOMP] SEND Frame 메시지 받는 곳
    @MessageMapping("/message")
    public void handleSendMessage(SendMessage sendMessage){
        logger.info("handleSendMessage 에서 메시지를 받았습니다. {}", sendMessage);
        chatMessageService.save(sendMessage);
    }



    // 유저의 전체 읽지 않은 메시지 개수
    @GetMapping("/user/unread/messages")
    public ResponseEntity<?> getUnReadMessages(Authentication authentication){

        logger.info("getUnReadMessages 의 요청들어옴");
        if (authentication != null) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            int unreadCnt = chatMessageService.findUnreadMessages(userDetails.getId());
            logger.info("userDetails.getId() : {}", userDetails.getId());
            logger.info("unreadMessageCnt : {}", unreadCnt);

            // Entity to Dto ( 서비스에서 바꾸고 나서 돌려주자 )
            ChatMessagesUnreadCountDto chatMessagesUnreadCountDto = new ChatMessagesUnreadCountDto();
            chatMessagesUnreadCountDto.setUnreadMessagesCnt(unreadCnt);

            // Dto -> apiResponseEntity
            ApiResponseEntity apiResponseEntity = ApiResponseEntity
                    .builder()
                    .message("유저의 읽지않은 메시지 개수")
                    .data(chatMessagesUnreadCountDto)
                    .status(200).build();

            return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
        } else {
            logger.info("authentication is null");
        }

        return null;
    }
}
