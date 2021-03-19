package com.springboot.dgumarket.controller.chat;

import com.springboot.dgumarket.dto.block.BlockStatusDto;
import com.springboot.dgumarket.dto.chat.ChatMessagesUnreadCountDto;
import com.springboot.dgumarket.exception.stomp.StompErrorException;
import com.springboot.dgumarket.payload.request.chat.SendMessage;
import com.springboot.dgumarket.payload.response.ApiResponseEntity;
import com.springboot.dgumarket.payload.response.stomp.error.StompErrorResponseMessage;
import com.springboot.dgumarket.service.awss3.AWSS3MultiImgService;
import com.springboot.dgumarket.service.UserDetailsImpl;
import com.springboot.dgumarket.service.block.UserBlockService;
import com.springboot.dgumarket.service.chat.ChatMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/chat")
public class ChatMessageController {
    private static final Logger logger = LoggerFactory.getLogger(ChatMessageController.class);

    @Autowired
    ChatMessageService chatMessageService;

    @Autowired
    UserBlockService userBlockService;

    @Autowired
    SimpMessagingTemplate template;

    // [STOMP] SEND Frame 메시지 받는 곳
    @MessageMapping("/message")
    @MessageCheckValidate
    public void handleSendMessage(SendMessage sendMessage, SimpMessageHeaderAccessor accessor) throws StompErrorException {
        BlockStatusDto blockStatus = userBlockService.checkBlockStatus(sendMessage.getSenderId(), sendMessage.getReceiverId());

        switch (blockStatus.getBlock_status()){
            case 1:
                throw StompErrorException.builder().ERR_CODE(1).ERR_MESSAGE("You block target user, so can't send message").build();
            case 2:
                throw StompErrorException.builder().ERR_CODE(2).ERR_MESSAGE("You blocked by user, so can't send message").build();
            case 3:
                String sessionId = accessor.getSessionId();
                chatMessageService.save(sendMessage, sessionId);
        }
    }

    // 유저의 전체 읽지 않은 메시지 개수
    @GetMapping("/unread/messages")
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

    // [STOMP] 메시지 예외처리
    @MessageExceptionHandler(StompErrorException.class)
    @SendToUser(destinations="/queue/error", broadcast = false)
    public StompErrorResponseMessage handleException(StompErrorException exception) {

        return StompErrorResponseMessage.builder()
                .error_code(exception.getERR_CODE())
                .error_description(exception.getERR_MESSAGE()).build();
    }
}
