package com.springboot.dgumarket.service.chat;


import com.springboot.dgumarket.dto.chat.ChatMessageDto;
import com.springboot.dgumarket.dto.chat.ChatMessageUserDto;
import com.springboot.dgumarket.dto.chat.ChatRoomProductDto;
import com.springboot.dgumarket.model.chat.ChatMessage;
import com.springboot.dgumarket.model.chat.ChatRoom;
import com.springboot.dgumarket.model.chat.RedisChatRoom;
import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.model.product.Product;
import com.springboot.dgumarket.payload.request.chat.SendMessage;
import com.springboot.dgumarket.payload.response.stomp.StompReceivedMessage;
import com.springboot.dgumarket.payload.response.stomp.StompRoomInfo;
import com.springboot.dgumarket.repository.chat.ChatMessageRepository;
import com.springboot.dgumarket.repository.chat.ChatRoomRepository;
import com.springboot.dgumarket.repository.member.MemberRepository;
import com.springboot.dgumarket.repository.product.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ChatMessageServiceImpl implements ChatMessageService {

    private static Logger logger = LoggerFactory.getLogger(ChatMessageServiceImpl.class);
    private static final int UNREAD = 0;
    private static final int READ = 1;

    @Autowired
    private SimpMessagingTemplate template;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private MemberRepository memberRepository;


    @Autowired
    private RedisChatRoomService redisChatRoomService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ChatRoomService chatRoomService;

    /** OK */
    // 로그인 유저의 모든 채팅방 대상, 읽지 않은 메시지 개수 조회
    @Override
    public Integer findUnreadMessages(int userId) {

        // 로그인 유저 (NPE 대상 X)
        Member loginUser = memberRepository.findById(userId);

        // 안 읽은 메시지 수 초기화
        int unreadMessages = 0;

        // 채팅방 리스트 중, 로그인 유저가 읽지 않은 메시지 수 조회
        // 파라미터 값 중 NPE 대상 없음
        List<ChatRoom> chatRoomList = chatRoomRepository.findChatRoomsByConsumerAndConsumerDeletedOrSellerAndSellerDeleted(
                loginUser, // 채팅방에서 로그인 유저가 구매자로 있는 경우
                0, // 로그인 유저 (= 구매자 입장)가 삭제하지 않은 채팅방 대상
                loginUser, // 채팅방에서 로그인 유저가 판매자로 있는 경우
                0 // 로그인 유저 (= 판매자 입장)가 삭제하지 않은 채팅방 대상
        );

        for(ChatRoom chatRoom : chatRoomList){
            // 개별 채팅방을 체크하면서,
            // 안 읽은 메시지 수 합
            unreadMessages += chatRoomService.calculateUnreadMessageCount(chatRoom, loginUser);
        }

        // 로그인 유저가 안 읽은 메시지 합 (반환 값)
        return unreadMessages;
    }

    /** OK */
    // 요청한 채팅방 고유 ID에 참조 걸려 있는 모든 메시지 조회
    // 채팅방 고유 ID (NPE 대상 X, 단 채팅방 고유 ID에 해당하는 로우를 삭제하지 않고, 해당 로우에 참조된 유저 정보, 상품 정보만 NULl 처리한다면)
    // 로그인 유저 고유 ID (NPE 대상 X)
    @Override
    public List<ChatMessageDto> getAllMessages(int roomId, Member loginUser) {

        // 채팅방 고유 ID를 통해 채팅방 객체 탐색 (NPE X)
        Optional<ChatRoom> chatRoom = chatRoomRepository.findById(roomId);

        // ModelMapper 객체 초기화
        ModelMapper modelMapper = new ModelMapper();
        // chatmessage entitiy -> message dto
        PropertyMap<ChatMessage, ChatMessageDto> messageMap = new PropertyMap<ChatMessage, ChatMessageDto>() {
            @Override
            protected void configure() {
                map().setMessage(source.getMessage());
                map().setMessageDate(source.getMsgDate());
            }
        };

        // member entity -> member dto
        PropertyMap<Member, ChatMessageUserDto> personMap = new PropertyMap<Member, ChatMessageUserDto>() {
            @Override
            protected void configure() {
                map().setUserId(source.getId());
                map().setNickName(source.getNickName());
                map().setProfileImgPath(source.getProfileImageDir());
            }
        };

        modelMapper.addMappings(messageMap);
        modelMapper.addMappings(personMap);


        List<ChatMessage> chatMessageEntitys = getRoomMessages(roomId, loginUser);
        // EntityList -> DTOList

        // 채팅 상대방 유저 (NPE 대상)
        Member opponentUser = chatRoom.get().getMemberOpponent(loginUser);

        // isTargetUserDisable() -> NPE 체크, 채팅 상대방이 NULL 인 경우 true 반환 -> 이름 없음으로 출력되도록 한다.
        Boolean isDisable = isTargerUserDisable(opponentUser);

        List<ChatMessageDto> chatMessageDtos = new ArrayList<>();
        for (ChatMessage chatMessageEntity : chatMessageEntitys) {
            ChatMessageUserDto chatMessageUserDto = modelMapper.map(chatMessageEntity.getSender(), ChatMessageUserDto.class);

            // 채팅 상대방 유저가 탈퇴 이후 실제 데이터베이스에서 삭제된 경우
            // 채팅 상대방 유저가, 유저제재 또는 탈퇴일 경우
            if (isDisable) {

                if ((opponentUser == null) || (chatMessageUserDto.getUserId() == opponentUser.getId())) {
                    chatMessageUserDto.setNickName("이름없음");
                    chatMessageUserDto.setProfileImgPath(null);
                }
            }
            ChatMessageDto chatMessageDto = new ChatMessageDto();
            chatMessageDto.setRoomId(chatMessageEntity.getRoomId());

            chatMessageDto.setChatMessageUserDto(chatMessageUserDto);

            chatMessageDto.setMessageDate(chatMessageEntity.getMsgDate());

            chatMessageDto.setMessage(chatMessageEntity.getMessage());

            chatMessageDto.setMessage_type(chatMessageEntity.getMsgType());

            chatMessageDto.setMessageStatus(chatMessageEntity.getMsgStatus());

            chatMessageDtos.add(chatMessageDto);
        }

        return chatMessageDtos;
    }


    /** OK
     * 호출 위치 : ChatMessageController save()
     *
     * save() 호출하는 곳에서 이미, 채팅 전송자, 수신자에 대한 유효성 검증을 완료한다.
     * 따라서, save() 메소드에서는 회원탈퇴로 인한 회원 정보 NPE 관련 예외처리르 할 필요 없다.
     * */
    @Override
    @Transactional
    public ChatMessageDto save(SendMessage sendMessage, String sessionId) {

        //--------------------------------------mapper---------------------------------------------------
        StompReceivedMessage responseMessage = new StompReceivedMessage();
        ModelMapper modelMapper = new ModelMapper();

        // chatmessage entitiy -> message dto
        PropertyMap<ChatMessage, StompReceivedMessage> messageMap = new PropertyMap<ChatMessage, StompReceivedMessage>() {
            @Override
            protected void configure() {
                map().setMessage(source.getMessage());
                map().setMessageDate(source.getMsgDate());
            }
        };

        // member entity -> member dto
        PropertyMap<Member, ChatMessageUserDto> personMap = new PropertyMap<Member, ChatMessageUserDto>() {
            @Override
            protected void configure() {
                map().setUserId(source.getId());
                map().setNickName(source.getNickName());
                map().setProfileImgPath(source.getProfileImageDir());
            }
        };
        // product entity -> product dto
        PropertyMap<Product, ChatRoomProductDto> productMap = new PropertyMap<Product, ChatRoomProductDto>() {
            @Override
            protected void configure() {
                map().setProduct_id(source.getId());
                map().setProduct_deleted(source.getProductStatus());
                map().setProductImgPath(source.getImgDirectory());
            }
        };
        modelMapper.addMappings(productMap);
        modelMapper.addMappings(messageMap);
        modelMapper.addMappings(personMap);

        Member receiver = memberRepository.findById(sendMessage.getReceiverId());
        Member sender = memberRepository.findById(sendMessage.getSenderId());
        Product product = productRepository.findById(sendMessage.getProductId());


        ChatMessage savedMessage = null;

        // 채팅방 찾기
        ChatRoom chatRoom =chatRoomRepository.findChatRoomPSR(sendMessage.getProductId(), sendMessage.getSenderId(), sendMessage.getReceiverId());
        LocalDateTime entranceDate = LocalDateTime.now();

        // 기존 생성된 채팅방이 있는 경우
        if (chatRoom != null) {

            // Redis 서버를 통해, 수신자가 채팅방에 위치하고 있는 지(채팅방에 위치하면, 바로 읽음 상태)
            Optional<RedisChatRoom> redisChatRoom = redisChatRoomService.findByRoomId(chatRoom.getRoomId());
            if (redisChatRoom.isPresent()) {
                // 상대방이 채팅방에 들어와있는 경우
                if(redisChatRoom.get().isSomeoneInChatRoom(String.valueOf(sendMessage.getReceiverId()))){
                    // 채팅방 나감 유무 1과 0은 레디스의 채팅방 나감유무와 별개이다

                    if (sendMessage.getMessageType() == 1) { // 채팅 이미지 메시지들 보낼 때
                        ArrayList<ChatMessage> chatMessages = createMessagesFromFileListAndChatRoom(sendMessage.getMessage(), chatRoom, sender, receiver, READ);
                        ArrayList<StompReceivedMessage> messagesDto = saveImageMessages2Dto(chatMessages, modelMapper);
                        for(StompReceivedMessage responseMesasge : messagesDto){ // 메시지 전송
                            sendMessagesToRoom(chatRoom.getRoomId(), responseMesasge);
                        }
                    } else { // 채팅 텍스트 메시지 보낼 때
                        savedMessage = createMessageFromSenderTextMessage(sendMessage, chatRoom, sender, receiver, READ, sendMessage.getMessageType());
                        responseMessage = saveTextMessage2Dto(savedMessage, modelMapper);
                        // 만약 상대방 나갔을 경우 나가기1 -> 나가기0
                        chatRoom.changeExitToJoin(sendMessage.getReceiverId());
                        // 로직 추가, 보내기 전에 만약 내가 해당 채팅방에서 나가기 상태(1) -> 나가지않은상태(0)으로 바꾸어 준다
                        chatRoom.leave2enterForFirstMessage(sender, entranceDate);
                        sendMessagesToRoom(responseMessage.getRoomId(), responseMessage); // 룸에 있는 사람에게 메시지전달
                    }
                } else {
                    // 상대방이 채팅방에 들어와 있지 않은 경우
                    if (sendMessage.getMessageType() == 1) { // 이미지 파일인경우
                        ArrayList<ChatMessage> chatMessages = createMessagesFromFileListAndChatRoom(sendMessage.getMessage(), chatRoom, sender, receiver, UNREAD);
                        ArrayList<StompReceivedMessage> messagesDto = saveImageMessages2Dto(chatMessages, modelMapper);
                        // 만약 상대방 나갔을 경우 나가기1 -> 나가기0
                        chatRoom.changeExitToJoin(sendMessage.getReceiverId());
                        // 로직 추가, 보내기 전에 만약 내가 해당 채팅방에서 나가기 상태(1) -> 나가지않은상태(0)으로 바꾸어 준다
                        chatRoom.leave2enterForFirstMessage(sender, entranceDate);

                        for(StompReceivedMessage responseMesasge : messagesDto){ // 메시지 전송
                            sendMessageToRoomAndUser(chatRoom.getRoomId(), receiver.getId(), responseMesasge);
                        }

                    } else {
                        ChatMessage chatMessage = createMessageFromSenderTextMessage(sendMessage, chatRoom, sender, receiver, UNREAD, sendMessage.getMessageType());

                        responseMessage = saveTextMessage2Dto(chatMessage, modelMapper);
                        // 만약 상대방 나갔을 경우 나가기1 -> 나가기0
                        chatRoom.changeExitToJoin(sendMessage.getReceiverId());
                        // 로직 추가, 보내기 전에 만약 내가 해당 채팅방에서 나가기 상태(1) -> 나가지않은상태(0)으로 바꾸어 준다
                        chatRoom.leave2enterForFirstMessage(sender, entranceDate);
                        sendMessageToRoomAndUser(responseMessage.getRoomId(), receiver.getId(), responseMessage); // 룸과 상대방에게 메시지 전달
                    }
                }
            }

        } else {
            // 새로운 채팅방을 생성해야 하는 경우
            ChatRoom newChatRoom = ChatRoom.builder()
                    .consumer(sender)
                    .seller(receiver)
                    .product(product)
                    .build();
            ChatRoom savedChatroom = chatRoomRepository.save(newChatRoom);
            ArrayList<StompReceivedMessage> receivedMessages = new ArrayList<>();


            if (sendMessage.getMessageType() == 1) { // 채팅으로 거래시 최초로 파일을 올리는 경우
                ArrayList<ChatMessage> chatMessages = createMessagesFromFileListAndChatRoom(sendMessage.getMessage(), savedChatroom, sender, receiver, 0);
                receivedMessages = saveImageMessages2Dto(chatMessages, modelMapper);
            } else {
                ChatMessage chatMessage = createMessageFromSenderTextMessage(sendMessage, savedChatroom, sender, receiver, UNREAD, 0);
                StompReceivedMessage stompReceivedMessage = saveTextMessage2Dto(chatMessage, modelMapper);
                this.template.convertAndSend("/topic/chat/" + sendMessage.getReceiverId(), stompReceivedMessage); // 상대방에게 채팅 메시지를 보낸다
            }
            // 모든 메시지 다 저장한 후에 채팅방 정보를 준다
            sendRoomInfoMsgToSender(savedChatroom.getRoomId(), sessionId);

            // [d1e6042fd4634a6ea3060cef6606523b.heic, dde5b6701a524386b432d8d134f2238c.jpeg, 40794ca3d33c453d944708deab241ec2.jpeg] -> 개별 메시지로 만듬
            // 상대방에게 메시지를 보냄(STOMP, SEND)
            if(sendMessage.getMessageType() == 1){
                receivedMessages.forEach(responseMsg -> this.template.convertAndSend("/topic/chat/" + sendMessage.getReceiverId(), responseMsg));
            }
        }

        return null;
    }

    /** OK */
    // 로그인 유저가 입장한 채팅방 대상으로, 로그인 유저의 해당 채팅방 입장 시간 기준으로
    // 메시지 리스트 반환 (채팅방 고유 ID, 로그인 유저 -> NPE 대상 X)
    // 채팅방 고유 ID -> 채팅방 생성 이후 호출되는 메소드, 로그인 유저 -> Gateway 서버에서 필터링)
    public List<ChatMessage> getRoomMessages(int roomId, Member loginUser) {

        ChatRoom chatRoom = chatRoomRepository.getOne(roomId);

        // getUsersEntranceDate(Member loginUser)
        // 판매자인지 구매자인지 찾고 판매자이면 판매자의입장시간, 구매자면 구매자의 입장시간
        LocalDateTime usersEntranceDate = chatRoom.getUsersEntranceDate(loginUser);

        // getUserleaveDate(Member loginUser)
        // 로그인 유저가 채팅방 나갈 때 요청 -> 로그인 유저가 구매자인지 판매자인지 체크 후 해당하는 컬럼에 나간 시간
        LocalDateTime usersRoomDeletedDate = chatRoom.getUserleaveDate(loginUser);


        if (usersEntranceDate == null && usersRoomDeletedDate == null) {
            log.error("[1] 로그인 유저에게 채팅 상대방이 메시지를 보냈지만, 로그인 유저가 한번도 접근하지 않은 방에 입장한 경우");
            // 로그인 유저에게 채팅 상대방이 메시지를 보냈지만,
            // 로그인 유저가 한번도 접근하지 않은 방에 입장한 경우
            List<ChatMessage> chatMessageList = chatMessageRepository.findChatMessagesByRoomIdOrderByMsgDate(roomId);
            return chatMessageList;

        } else if (usersEntranceDate == null && usersRoomDeletedDate != null) {
            log.error("[2] 로그인 유저가 한번도 접근하지 않은 상태에서, 해당 채팅방에 나갔었던 방에 입장한 경우");
            // 로그인 유저가 한번도 접근하지 않은 상태에서,
            // 해당 채팅방에 나갔었던 방에 입장한 경우
            List<ChatMessage> chatMessageList = chatMessageRepository.findChatMessagesByRoomIdAndMsgDateIsAfterOrderByMsgDateAsc(roomId, usersRoomDeletedDate);
            return chatMessageList;

        } else if (usersEntranceDate != null && usersRoomDeletedDate == null) {
            log.error("[3] 로그인 유저가 해당 채팅방에 입장했었고, 해당 채팅방에서 나간 적이 없는 방에 입장한 경우");
            // 로그인 유저가 해당 채팅방에 입장했었고,
            // 해당 채팅방에서 나간 적이 없는 방에 입장한 경우
            List<ChatMessage> chatMessageList = chatMessageRepository.findChatMessagesByRoomIdOrderByMsgDate(roomId);
            return chatMessageList;

        } else {
            log.error("[4] 로그인 유저가 해당 채팅방에 입장했었고, 해당 채팅방에서 나간 적이 있는 방에 입장한 경우");
            // 로그인 유저가 해당 채팅방에 입장했었고,
            // 해당 채팅방에서 나간 적이 있는 방에 입장한 경우
            List<ChatMessage> chatMessageList = chatMessageRepository.findChatMessagesByRoomIdAndMsgDateIsAfterOrderByMsgDateAsc(roomId, usersRoomDeletedDate);
            return chatMessageList;
        }
    }

    /**
     * create chat_message to be inserted to message table(db)
     *
     * procedure
     * 1. parse filelist
     * 2. create message with filename parsed from filelist
     *
     * @param fileListStrFromMessage
     * @param chatRoom
     * @return ArrayList<ChatMessage>, new chatMessages entity before inserted db
     */

    // 호출 : sava() 메소드
    public ArrayList<ChatMessage> createMessagesFromFileListAndChatRoom(
            String fileListStrFromMessage, ChatRoom chatRoom,
            Member sender,
            Member receiver,
            int readStatus){
        String[] fileList = fileListStrFromMessage.replace("[", "") // 파싱
                .replace("]", "")
                .split(", ");
        ArrayList<ChatMessage> chatMessageArrayList = new ArrayList<>();
        for (String s : fileList) {
            ChatMessage chatMessage = ChatMessage.builder() // 메시지 만들기
                    .message(s)
                    .msgDate(LocalDateTime.now())
                    .msgStatus(readStatus) // 읽음유무
                    .msgType(1) // 메시지 타입
                    .receiver(receiver) // 받는 이
                    .sender(sender) // 보내는 이
                    .product(chatRoom.getProduct()) // 물건번호
                    .roomId(chatRoom.getRoomId()).build(); // 방번호
            chatMessageArrayList.add(chatMessage);
        }
        return chatMessageArrayList;
    }

    // 호출 : sava() 메소드
    public ChatMessage createMessageFromSenderTextMessage(SendMessage sendMessage, ChatRoom chatRoom, Member sender, Member receiver, int readStatus, int messageType){
        ChatMessage chatMessage = ChatMessage.builder()
                .message(sendMessage.getMessage())
                .roomId(chatRoom.getRoomId())
                .sender(sender)
                .receiver(receiver)
                .msgStatus(readStatus)
                .msgType(messageType)
                .product(chatRoom.getProduct()).build();
        return chatMessage;
    }

    /**
     * save Image Messages and messages entity list to dto, return message dtos
     * @param chatMessages : message to be inserted to db
     * @param modelMapper : model mapper
     * @return message dtos
     */
    // 호출 : sava() 메소드
    public ArrayList<StompReceivedMessage> saveImageMessages2Dto(ArrayList<ChatMessage> chatMessages, ModelMapper modelMapper){
        List<ChatMessage> chatMessageSaved = chatMessageRepository.saveAll(chatMessages); // saveImageMessages
        ArrayList<StompReceivedMessage> stompReceivedMessages = new ArrayList<>();

        for(ChatMessage msg : chatMessageSaved){ // to dto
            StompReceivedMessage responseMessage = new StompReceivedMessage();
            responseMessage.setChatMessageUserDto(modelMapper.map(msg.getSender(), ChatMessageUserDto.class)); // 보내는이 정보
            responseMessage.setChatRoomProductDto(modelMapper.map(msg.getProduct(), ChatRoomProductDto.class));
            responseMessage.setRoomId(msg.getRoomId());
            responseMessage.setMessageDate(msg.getMsgDate());
            responseMessage.setMessage(msg.getMessage());
            responseMessage.setMessage_type(msg.getMsgType());
            responseMessage.setMessageStatus(msg.getMsgStatus());
            stompReceivedMessages.add(responseMessage);
        }
        return stompReceivedMessages;
    }

    /**
     *  save text Messages and messages entity to dto, return message dto
     * @param chatMessage
     * @param modelMapper
     * @return
     */
    // 호출 : sava() 메소드
    public StompReceivedMessage saveTextMessage2Dto(ChatMessage chatMessage, ModelMapper modelMapper){
        ChatMessage msg = chatMessageRepository.save(chatMessage);
        StompReceivedMessage responseMessage = new StompReceivedMessage();
        responseMessage.setChatMessageUserDto(modelMapper.map(msg.getSender(), ChatMessageUserDto.class)); // 보내는이 정보
        responseMessage.setChatRoomProductDto(modelMapper.map(msg.getProduct(), ChatRoomProductDto.class));
        responseMessage.setRoomId(msg.getRoomId());
        responseMessage.setMessageDate(msg.getMsgDate());
        responseMessage.setMessage(msg.getMessage());
        responseMessage.setMessage_type(msg.getMsgType());
        responseMessage.setMessageStatus(msg.getMsgStatus());
        return responseMessage;
    }


    /**
     * send message to destination(send), through stomp protocol
     * send to destination
     * 1. /topic/room/{roomId}, send message to specific room(I already entered)
     * 2. /topic/chat/{receiverId} , send messages to receiver (global)
     *
     * @param roomId room id
     * @param receiverId receiver id
     * @param responseMessage
     */
    // 호출 : sava() 메소드
    public void sendMessageToRoomAndUser(int roomId, int receiverId, StompReceivedMessage responseMessage){
        this.template.convertAndSend("/topic/room/" + roomId, responseMessage); // 룸으로 메시지 전달
        this.template.convertAndSend("/topic/chat/" + receiverId, responseMessage); // 상대방에게 메시지 전달
    }

    /**
     * send message to destination(send), through stomp protocol
     * send to destination
     * 1. /topic/room/{roomId}, send message to specific room(I already entered)
     *
     * if receiver already enter room, you don't have to send a message to receiver directly by destination '/topic/chat/{receiverId}'
     * in this case you sendMessageToRoom(method)
     *
     * @param roomId
     * @param responseMessage
     */
    // 호출 : sava() 메소드
    public void sendMessagesToRoom(int roomId, StompReceivedMessage responseMessage){
        this.template.convertAndSend("/topic/room/" + roomId, responseMessage); // 룸으로 메시지 전달
    }

    // 호출 : sava() 메소드
    public void sendRoomInfoMsgToSender(int roomId, String sessionId){
        // 모든 메시지 다 저장한 후에 채팅방 정보를 준다.
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);
        StompRoomInfo stompRoomInfo = new StompRoomInfo(String.valueOf(roomId));
        this.template.convertAndSendToUser(sessionId,"/queue/room/event", stompRoomInfo, headerAccessor.getMessageHeaders());
    }

    /** OK */
    // 채팅 상대방이 탈퇴 또는 유저제재일 경우를 체크함.
    public boolean isTargerUserDisable(Member opponentUser) {

        // 채팅 상대 유저가 탈퇴 후, 실제 데이터베이스에서 삭제되는 경우 true 반환
        if (opponentUser == null) return true;

        // 채팅 상대 유저가 탈퇴 또는 이용제재 상태인 경우 true 반환
        if (opponentUser.getIsWithdrawn() == 1 || opponentUser.getIsEnabled() == 1) {
            return true;
        }
        return false;
    }
}
