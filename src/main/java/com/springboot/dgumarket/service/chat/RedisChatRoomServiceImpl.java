package com.springboot.dgumarket.service.chat;

import com.springboot.dgumarket.dto.chat.ChatMessageDto;
import com.springboot.dgumarket.model.chat.ChatRoom;
import com.springboot.dgumarket.model.chat.RedisChatRoom;
import com.springboot.dgumarket.model.chat.RedisChatUser;
import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.repository.chat.ChatMessageRepository;
import com.springboot.dgumarket.repository.chat.ChatRoomRepository;
import com.springboot.dgumarket.repository.chat.RedisChatRoomRepository;
import com.springboot.dgumarket.repository.member.MemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RedisChatRoomServiceImpl implements RedisChatRoomService{
    private static Logger logger = LoggerFactory.getLogger(RedisChatRoomServiceImpl.class);

    @Autowired
    ChatRoomRepository chatRoomRepository;

    @Autowired
    RedisChatRoomRepository redisChatRoomRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ChatMessageRepository chatMessageRepository;

    @Autowired
    ChatMessageService chatMessageService;

    @Autowired
    SimpMessagingTemplate template;

    @Autowired
    StringRedisTemplate redisTemplate;

    // 방찾기
    @Override
    public Optional<RedisChatRoom> findByRoomId(int roomId) {
        return redisChatRoomRepository.findById(String.valueOf(roomId));
    }


    // 채팅방 들어가기 ( 채팅방목록에서 채팅방화면 들어갈 경우)
    @Override
    public RedisChatRoom join(int roomId, int senderId, String sessionId) {
        Member member = memberRepository.findById(senderId); // stomp header 있는 사용자 아이디로 유저 찾기
        logger.info("find user by userId in stomp header : {}", member.getId());


        Optional<RedisChatRoom> optionalRedisChatRoom = redisChatRoomRepository.findById(String.valueOf(roomId));
        RedisChatRoom redisChatRoom;


        if(optionalRedisChatRoom.isPresent()){ // 채팅방 존재 하는 경우
            logger.info("[REDIS, [ROOM][JOIN]] already chat room existed");
            logger.info("[REDIS, [ROOM][JOIN]] get connectedUsers");
            redisChatRoom = optionalRedisChatRoom.get();
            redisChatRoom.getConnectedUsers().forEach(e -> logger.info("[A REDIS ROOM's user] : {}", e.toString()));
        }else { // Redis 채팅방 존재 X
            redisChatRoom = RedisChatRoom.builder().roomId(String.valueOf(roomId)).build();
            logger.info("[REDIS, [ROOM][JOIN]] chat room not existed");
            logger.info("[REDIS, [ROOM][JOIN]] create new chat room : {}", redisChatRoom.toString());
        }


        RedisChatUser redischatUser = RedisChatUser.builder().sessionId(sessionId).userId(senderId).build();
        redisChatRoom.addUser(redischatUser); // 채팅방에 유저 추가
        RedisChatRoom chatRoomAfterJoined = redisChatRoomRepository.save(redisChatRoom);
        chatRoomAfterJoined.getConnectedUsers().stream().forEach((e) -> logger.info("[REDIS, [ROOM][JOIN]] after join the room {}", e.toString()));
        redisTemplate.opsForValue().set(sessionId, String.valueOf(roomId)); // 레디스에 유저 세션정보 추가
        logger.info("sessionId : {}, roomId : {}", sessionId, roomId);



        logger.info("[[REDIS] {} 번에 가입된 유저들 : {}]", chatRoomAfterJoined.getRoomId(), chatRoomAfterJoined.getConnectedUsers().toString());
        // 읽지 않은 메시지 읽음 상태로 바꾸기(사용자 입장날짜 기준 뒤로)
        int num = chatMessageRepository.updateReadstatus(roomId, senderId);


        logger.info("[MYSQL, [ROOM][JOIN]] get number of updated message status(0 -> 1) : {}", num);
        if (num >= 0){
            // 상대방이 현재 채팅방에 있다?
            if(redisChatRoom.getConnectedUsers().stream().filter(e -> e.getUserId() != Integer.valueOf(senderId)).count() != 0){ // 상대방이 현재 채팅방 화면에 있다면
                int opponentId = redisChatRoom.getConnectedUsers().stream().filter(e -> e.getUserId() != Integer.valueOf(senderId)).findFirst().get().getUserId();
                logger.info("[[ROOM][JOIN]] someone is in chat room except me : {}", opponentId);
                this.template.convertAndSend("/topic/room/" + roomId + "/" + opponentId, "{\"who\" : \""+senderId+"\", \"event\" : \"join\" }"); // 상대방에게 실시간으로 읽은 사실을 알려줌
                logger.info("[[ROOM][JOIN]] [SEND] /topic/room/{}/{}, messages : {}", roomId, opponentId,"{\"who\" : \""+senderId+"\", \"event\" : \"join\" }");
            }else {
                logger.info("[[ROOM][JOIN]] someone is not in chat room");
            }
        }
        // 채팅방의 메시지들 조회하기
        List<ChatMessageDto> chatMessageDtos = chatMessageService.getAllMessages(roomId, member);

        // 입장할 떄 사용자 입장날짜 갱신하기
        Optional<ChatRoom> chatRoom = chatRoomRepository.findById(roomId);
        ChatRoom chatroom;
        if(chatRoom.isPresent()){
            chatroom = chatRoom.get();
            chatroom.updateEntranceDate(senderId);
            chatRoomRepository.save(chatroom);
        }

        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor
                .create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);
        template.convertAndSendToUser(sessionId,"/queue/room/event", chatMessageDtos, // 채팅방의 메시지들 전송
                headerAccessor.getMessageHeaders());
        logger.info("[[ROOM][JOIN]] [SEND] /queue/room/event, sessionId : {}, messages : {}", sessionId, chatMessageDtos);

        return null;
    }

    // 레디스 채팅방에서 유저 나가게 하기
    @Override
    public RedisChatRoom leave(String roomId, String sesionId) {
        Optional<RedisChatRoom> chatRoom = redisChatRoomRepository.findById(roomId);
        RedisChatRoom chatroom;

        if(chatRoom.isPresent()){
            chatroom = chatRoom.get();
            chatroom.removeUser(sesionId);
            logger.info("[ROOM][LEAVE] [SUB] after removeUser , {}", chatroom);
            if(chatroom.getNumberOfConnectedUsers() == 0){ // 모두다 나갔다면 채팅방을 지운다.
                redisChatRoomRepository.delete(chatroom);
            }else{
                RedisChatRoom redisChatRoom = redisChatRoomRepository.save(chatroom);
                logger.info("[ROOM][LEAVE] [SUB] after result {} ", redisChatRoom);
            }
        }else {
            logger.info("나가려고 하는 데 방이 없다!!");
        }
        return null;
    }

    // 레디스 채팅방 나가기( 갑작스러운 종료에 대한 대비 )
    @Override
    @Transactional
    public void leave(String sessionId) {
        /**
         * TODO: 채팅방 나갈떄에 대한 예외처리!
         */
        String room_id = redisTemplate.opsForValue().get(sessionId);
        if (room_id != null){ // 채팅방 존재한다면
            this.leave(room_id, sessionId); // 레디스 채팅방 나가기
            redisTemplate.delete(sessionId); // 해당 키 삭제
            logger.info("레디스 채팅방 나가기 , 받은 sessionId : {}", sessionId);
            logger.info("room id : {}, sessionId : {}", room_id, sessionId);
        }
    }

}
