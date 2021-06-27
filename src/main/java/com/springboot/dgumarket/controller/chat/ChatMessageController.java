package com.springboot.dgumarket.controller.chat;

import com.springboot.dgumarket.dto.block.BlockStatusDto;
import com.springboot.dgumarket.dto.chat.ChatMessagesUnreadCountDto;
import com.springboot.dgumarket.exception.stomp.StompErrorException;
import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.payload.request.chat.SendMessage;
import com.springboot.dgumarket.payload.response.ApiResponseEntity;
import com.springboot.dgumarket.payload.response.stomp.error.StompErrorResponseMessage;
import com.springboot.dgumarket.repository.member.MemberRepository;
import com.springboot.dgumarket.service.UserDetailsImpl;
import com.springboot.dgumarket.service.block.UserBlockService;
import com.springboot.dgumarket.service.chat.ChatMessageService;
import lombok.extern.slf4j.Slf4j;
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



@RestController
@RequestMapping("/api/chat")
@Slf4j
public class ChatMessageController {
    private static final Logger logger = LoggerFactory.getLogger(ChatMessageController.class);

    @Autowired
    ChatMessageService chatMessageService;

    @Autowired
    UserBlockService userBlockService;

    @Autowired
    SimpMessagingTemplate template;

    @Autowired
    MemberRepository memberRepository;

    // [STOMP] SEND Frame 메시지 받는 곳
    @MessageMapping("/message")
    public void handleSendMessage(SendMessage sendMessage, SimpMessageHeaderAccessor accessor) {

        // NPW 대상 X
        // 메시지 전송 시점에는 이미 인증된 유저임을 전제로 하고 있기 때문에 @MessageMapping에 요청이 할당되는 시점에는
        // 회원 정보가 유효함.
        Member loginUser = memberRepository.findById(sendMessage.getSenderId());

        if (loginUser == null || loginUser.getIsWithdrawn() == 1) {
            throw StompErrorException.builder().ERR_CODE(3).ERR_MESSAGE("탈퇴하거나, 존재하지 않는 유저는 메시지기능을 이용하실 수 없습니다.").build();
        }

        if (loginUser.getIsEnabled() == 1) {
            throw StompErrorException.builder().ERR_CODE(4).ERR_MESSAGE("관리자로부터 이용제재를 받고있습니다. 더 이상 서비스를 이용하실 수 없습니다.").build();
        }

        // NPE 대상
        // 회원탈퇴 이후, 회원정보가 삭제되는 경우 NULL
        Member opponentUser = memberRepository.findById(sendMessage.getReceiverId());

        // 채팅 상대방 정보 NPE 체크
        if (opponentUser == null) {
            throw StompErrorException.builder().ERR_CODE(5).ERR_MESSAGE("탈퇴하거나 존재하지 않는 유저에게 메시지를 보낼 수 없습니다.").build();
        }

        if (opponentUser.getIsWithdrawn() == 1) {
            throw StompErrorException.builder().ERR_CODE(5).ERR_MESSAGE("탈퇴하거나 존재하지 않는 유저에게 메시지를 보낼 수 없습니다.").build();
        }

        if (opponentUser.getIsEnabled() == 1) { // 관리자부터 제재를 받고 있는 유저에게 메시지 전송
            throw StompErrorException.builder().ERR_CODE(6).ERR_MESSAGE("관리자로부터 제재를 받고있는 유저에게 메시지를 전달할 수 없습니다.").build();
        }

        BlockStatusDto blockStatus = userBlockService.checkBlockStatus(sendMessage.getSenderId(), sendMessage.getReceiverId());

        switch (blockStatus.getBlock_status()){
            case 1:
                throw StompErrorException.builder().ERR_CODE(1).ERR_MESSAGE("내가 차단한 유저에게 채팅 메시지를 전송할 수 없습니다.").build();
            case 2:
                throw StompErrorException.builder().ERR_CODE(2).ERR_MESSAGE("나를 차단한 유저에게 채팅 메시지를 전송할 수 없습니다.").build();
            case 3:
                log.info("[메시지전송] 유저 고유 아이디 : {}, 메시지전송, sessionId : {}", sendMessage.getSenderId(), accessor.getSessionId());
                String sessionId = accessor.getSessionId();
                chatMessageService.save(sendMessage, sessionId);
        }
    }

    // 유저의 전체 읽지 않은 메시지 개수
    @GetMapping("/unread/messages")
    public ResponseEntity<?> getUnReadMessages(Authentication authentication){

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        int unreadCnt = chatMessageService.findUnreadMessages(userDetails.getId());

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
