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
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private RedisChatRoomRepository redisChatRoomRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private SimpMessagingTemplate template;

    @Autowired
    private StringRedisTemplate redisTemplate;

    // 방찾기
    @Override
    public Optional<RedisChatRoom> findByRoomId(int roomId) {
        return redisChatRoomRepository.findById(String.valueOf(roomId));
    }

    // 채팅방 목록 화면 -> 채팅 화면으로 클릭 후 입장하는 시점
    @Override
    public void join(int roomId, int senderId, String sessionId) {

        // 로그인 유저
        Member loginUser = memberRepository.findById(senderId);

        Optional<RedisChatRoom> optionalRedisChatRoom = redisChatRoomRepository.findById(String.valueOf(roomId));
        RedisChatRoom redisChatRoom;


        if (optionalRedisChatRoom.isPresent()) {
            // Redis Server에 요청한 채팅방 고유 ID에 대한 채팅방 정보가 존재하는 경우
            redisChatRoom = optionalRedisChatRoom.get();
            redisChatRoom.getConnectedUsers().forEach
                    (users -> logger.info(roomId + "번(고유 ID) 방에 소속된 유저 정보(유저 정보 업데이트 전) : {}", users.toString()));
        } else {
            // Redis Server에 요청한 채팅방 고유 ID에 대한 채팅방 정보가 존재하지 않은 경우
            redisChatRoom = RedisChatRoom.builder()
                    .roomId(String.valueOf(roomId))
                    .build();
        }


        // 해당 채팅방에 입장한 유저 (=로그인 유저) 객체 생성 (-> 해당 채팅방에 입장한 상태로 변경 위함)
        RedisChatUser redischatUser = RedisChatUser.builder()
                .sessionId(sessionId)
                .userId(senderId)
                .build();

        // Redis Server 채팅방에 로그인 유저가 입장한 상태로 변경
        redisChatRoom.addUser(redischatUser);
        RedisChatRoom chatRoomAfterJoined = redisChatRoomRepository.save(redisChatRoom);


        chatRoomAfterJoined.getConnectedUsers().stream().forEach(
                user -> logger.info(roomId + "번(고유 ID) 방에 소속된 유저 정보(로그인 유저 정보 업데이트 이후) : {}", user.toString()));

        // Redis Server에 로그인 유저의 세션 정보 설정
        redisTemplate.opsForValue().set(sessionId, String.valueOf(roomId));

        // 읽지 않은 메시지 읽음 상태로 바꾸기 (사용자 입장날짜 기준)
        int num = chatMessageRepository.updateReadstatus(roomId, senderId);
        logger.info("로그인 유저가 입장한 시간 기준으로, 안 읽음 -> 읽음 상태로 바뀐 메시지 객체 수 : {}", num);


        // 로그인 유저가 입장한 채팅방에 채팅 상대방 유저가 입장한 상태인 경우 (-> 로그인 유저의 입장 사실을 전송한다)
        if (redisChatRoom.getConnectedUsers().stream().filter(e -> e.getUserId() != Integer.valueOf(senderId)).count() != 0){

            // 채팅 상대방 고유 ID
            int opponentId = redisChatRoom.getConnectedUsers().stream().filter(e -> e.getUserId() != Integer.valueOf(senderId)).findFirst().get().getUserId();
            // 채팅 상대방에게 로그인 유저가 채팅방 입장 상태임을 전송
            this.template.convertAndSend("/topic/room/" + roomId + "/" + opponentId, "{\"who\" : \""+senderId+"\", \"event\" : \"join\" }");
            logger.info("[SEND Frame][JOIN] 채팅 상대방에게 로그인 유저의 채팅방 입장 상태임을 전송 -->  /topic/room/{}/{}, messages : {}", roomId, opponentId,"{\"who\" : \""+senderId+"\", \"event\" : \"join\" }");
        }


        // getAllMessages(int 채팅방 고유 번호, Member 로그인 유저)
        // 로그인 유저가 입장한 채팅방에 있는 모든 메시지 조회
        List<ChatMessageDto> chatMessageDtos = chatMessageService.getAllMessages(roomId, loginUser);

        // 채팅방 고유 ID를 통해 ChatRoom 객체 조회
        // NPE 대상
        ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId);
        // 로그인 유저가 입장한 시간 최신화
        chatRoom.updateEntranceDate(senderId);


        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);

        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);
        // 해당 채팅방에 포함된 모든 메시지 정보를 로그인 유저에게 전송
        template.convertAndSendToUser(sessionId,"/queue/room/event", chatMessageDtos, headerAccessor.getMessageHeaders());
        logger.info("[SEND Frame][JOIN] 채팅방에 포함된 모든 메시지 정보 로그인 유저에게 전송 --> /queue/room/event, sessionId : {}, messages : {}", sessionId, chatMessageDtos);
    }

    // 채팅방에서 < (뒤로가기 버튼), X 버튼, 크롬 브라우저 종료 시점
    // 다른 ___ 상황에서도 leave() 메소드 호출되면, 윗 줄에 추가해서 기록할 것.
    @Override
    public void leave(String roomId, String sesionId) {
        // [TRY] RedisChatRoomRepository에서 findByRoomId() 를 통해 Optional 안 쓰고 진행 했는데,
        // NPE 발생
        Optional<RedisChatRoom> optionalChatRoom = redisChatRoomRepository.findById(roomId);

        // 로그인 유저의 세션 ID를 통해 채팅방 객체에서 로그인 유저 정보를 삭제
        RedisChatRoom chatRoom = optionalChatRoom.get();
        chatRoom.removeUser(sesionId);
        logger.info("로그인 유저의 채팅화면에서 벗어난 이후, Redis 서버에 저장된 채팅방 객체 상태 : {}", chatRoom);

        // Redis 서버에 저장된 채팅방 객체에 참조된 유저 수가 0인 경우 (= 해당 채팅방에 입장한 유저가 없는 경우)
        if (chatRoom.getNumberOfConnectedUsers() == 0) {
            // Redis 서버에서 해당 채팅방 객체를 삭제한다.
            redisChatRoomRepository.delete(chatRoom);
        } else {
            // 로그인 유저 외 채팅 상대 유저가 채팅화면에 남아 있는 경우, 최신화된 채팅방 객체 상태를 Redis 서버에 저장
            RedisChatRoom redisChatRoom = redisChatRoomRepository.save(chatRoom);
            logger.info("채팅방에 모든 유저가 입장한 상태에서, 로그인 유저가 방에서 벗어난 이후 Redis 서버에 저장된 채팅방 객체 상태 : {}", redisChatRoom);
        }

    }

    // configureClientInboundChannel() on WebSocketConfig.class
    @Override
    @Transactional
    public void leave(String sessionId) {
        /**
         * TODO: 채팅방 나갈 때에 대한 예외처리!
         * configureClientInboundChannel() on WebSocketConfig.class
         */
        // 세션 ID 정보를 통해 로그인 유저가 입장한 상태로 있는 채팅방 고유 ID 조회
        String roomId = redisTemplate.opsForValue().get(sessionId);

        // 로그인 유저가 입장한 채팅방 정보가 있는 경우
        if (roomId != null) {

            // 해당 채팅 화면에서 벗어난 상태로 처리
            this.leave(roomId, sessionId);

            // 세션 ID 삭제
            redisTemplate.delete(sessionId);
        }
    }

}
