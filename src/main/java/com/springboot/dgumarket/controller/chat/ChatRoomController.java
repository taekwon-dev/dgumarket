package com.springboot.dgumarket.controller.chat;

import com.springboot.dgumarket.controller.product.ProductValidationForChatRoom;
import com.springboot.dgumarket.dto.chat.*;
import com.springboot.dgumarket.exception.CustomControllerExecption;
import com.springboot.dgumarket.payload.request.chat.ChatRoomLeaveRequest;
import com.springboot.dgumarket.payload.request.chat.ValidationRequest;
import com.springboot.dgumarket.payload.request.product.ProductStatusChangeRequest;
import com.springboot.dgumarket.payload.response.ApiResponseEntity;
import com.springboot.dgumarket.repository.member.MemberRepository;
import com.springboot.dgumarket.service.UserDetailsImpl;
import com.springboot.dgumarket.service.Validation.ValidationService;
import com.springboot.dgumarket.service.chat.ChatRoomService;
import com.springboot.dgumarket.service.chat.RedisChatRoomService;
import com.springboot.dgumarket.service.product.ProductReviewService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/chatroom")
public class ChatRoomController {
    private static Logger logger = LoggerFactory.getLogger(ChatMessageController.class);

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ChatRoomService chatRoomService;

    @Autowired
    private RedisChatRoomService redisChatRoomService;

    @Autowired
    private ProductReviewService productReviewService;

    @Autowired
    private ValidationService validationService;

    // 채팅방 목록 조회
    // Gateway JWTFilter를 통해 인증절차를 통과한 경우 (= 인증을 전제할 수 있다.)
    // Dgumarket 서버로 직접 요청을 하는 경우 역시, Interceptor에서 인증 여부를 체크한다. (=안전장치)
    @GetMapping("/lists")
    public ResponseEntity<List<ChatRoomListDto>> GetRoomList(Authentication authentication) {

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // 채팅방 목록에 렌더링 할 DTO
        List<ChatRoomListDto> chatRoomListDtos = chatRoomService.findAllRoomsByUserId(userDetails.getId());

        // 최신순으로 조회
        Comparator<ChatRoomListDto> compareByRecentChatRoomDate = (
                ChatRoomListDto o1, ChatRoomListDto o2) -> o1.getChatRoomRecentMessageDto().getMessage_date().compareTo( o2.getChatRoomRecentMessageDto().getMessage_date());
        Collections.sort(chatRoomListDtos, compareByRecentChatRoomDate.reversed());

        return new ResponseEntity<>(chatRoomListDtos, HttpStatus.OK);
    }

    // 채팅방 찾기
    @GetMapping("/find/room")
    public ResponseEntity<?> findChatRoom(
            @RequestParam(value = "productId")int productId,
            @RequestParam(value = "sellerId")int sellerId,
            @RequestParam(value = "userId")int userId){

        ChatRoomCheckExistedDto chatRoomCheckExistedDto = chatRoomService.findChatRoomByProductSellerConsumer(productId, sellerId, userId);

        ApiResponseEntity apiResponseEntity = ApiResponseEntity
                .builder()
                .status(200)
                .message("채팅방 유무 확인")
                .data(chatRoomCheckExistedDto).build();
        return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
    }


    // 채팅방상단 물건의 대략적인 정보 + 채팅방이 내것인지 확인 가져옴 ( 기존 채팅방 ), 1.13 완료 ( API 문서작성 완료 & 피드백 완료 ) -> 수정
    @GetMapping("/product/{productId}")
    @ProductValidationForChatRoom
    public ResponseEntity<?> getChatRoomProductInfo(
            Authentication authentication,
            @PathVariable("productId") int productId) throws CustomControllerExecption {


        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // NPE 대상
        ChatRoomSectionProductDto chatRoomSectionProductDto = chatRoomService.findRoomProductSectionByProduct(productId, userDetails.getId());
        ApiResponseEntity apiResponseEntity = ApiResponseEntity
                .builder()
                .status(200)
                .message("채팅방 물건 정보")
                .data(chatRoomSectionProductDto).build();

        return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);


    }

    // 채팅방에서 거래완료 요청 보내기 ( API 문서작성 완료 & 피드백 완료 )
    @PatchMapping("/{roomId}/trade-done")
    public ResponseEntity<?> updateProductStatus (
            Authentication authorization,
            @RequestBody ProductStatusChangeRequest statusChangeRequest,
        @PathVariable("roomId") int roomId) throws CustomControllerExecption {

        if(authorization != null) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authorization.getPrincipal();
            chatRoomService.changeRoomTransactionStatus(userDetails.getId(), roomId, statusChangeRequest.getTransaction_status_id());
            return ResponseEntity.ok("product status updated");
        }

        return null;
    }

    // 채팅방 상태 확인하기
    @GetMapping("/{roomId}/status")
    public ResponseEntity<?> checkRoom(@PathVariable("roomId") int roomId, Authentication authentication) {
        if (authentication != null){
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            ChatRoomStatusDto chatRoomStatusDto = chatRoomService.getChatRoomStatus(roomId, userDetails.getId());
            ApiResponseEntity apiResponseEntity = ApiResponseEntity
                    .builder()
                    .status(200)
                    .message("채팅방 정보")
                    .data(chatRoomStatusDto).build();
            return new ResponseEntity<>(apiResponseEntity, HttpStatus.OK);
        }
        return null;
    }

    // 채팅방 나가기
    @PatchMapping("/leave/{roomId}")
    public ResponseEntity<?> leaveChatRoom(
            @PathVariable("roomId") int roomId,
            @RequestBody ChatRoomLeaveRequest roomLeaveRequest,
            Authentication authentication){

        /** @RequestBody NULL 체크를 하는 이유? */
        if (roomLeaveRequest != null) {

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            if(roomLeaveRequest.isRoom_leave()){

                // 데이터베이스에서 물리 데이터를 삭제하는 것이 아닌,
                // 상태 값을 0 -> 1로 변경함으로써 삭제 상태임을 표현
                chatRoomService.leaveChatRoom(roomId, userDetails.getId());
                return ResponseEntity.ok("room leave success");
            }
        }

        return null;
    }



    // 채팅방 구독 -> REDIS 채팅방 가입 ( API 문서작성 완료 & 피드백 완료 )
    @SubscribeMapping("/topic/room/{roomId}/{userId}")
    public void joinChatRoom(@DestinationVariable String roomId, @DestinationVariable String userId, Message<?> message){
        logger.info("[SUB] - destination : room/join/{}/{}, message's header : {}",roomId, userId, message.getHeaders());
        logger.info("[SUB] - sessionId : {}", message.getHeaders().get("simpSessionId"));
        logger.info("[SUB] - userId : {}", message.getHeaders().get("nativeHeaders"));
        String sessionId = message.getHeaders().get("simpSessionId").toString(); // ## 개별 웹소켓 세션 아이디 정보를 redis 채팅방에 가입시 활용합니다.
        redisChatRoomService.join(Integer.valueOf(roomId), Integer.valueOf(userId), sessionId); // 구독오면 redis 채팅방 가입
    }


    // ------------------------------------- [ 유저제재 / 탈퇴 / 차단 / 물건블라인드 체크 ] -----------------------------------

    // 채팅방에서 (제재, 프로필 차단) 유저프로필 또는 물건이미지 클릭시 먼저 유효성 체크 ( 3/10 )
    @PostMapping("/check-validate")
    public String validationCheck(
            Authentication authentication,
            @RequestBody ValidationRequest validationRequest) throws CustomControllerExecption {


        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return validationService.checkValidateForChatroom(userDetails.getId(), validationRequest);

    }
}
