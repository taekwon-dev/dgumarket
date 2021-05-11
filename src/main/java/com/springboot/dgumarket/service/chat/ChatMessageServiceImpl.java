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

    // 읽지 않은 메시지 개수
    @Override
    public Integer findUnreadMessages(int userId) {
        Member member = memberRepository.findById(userId);
        int unreadMessages = 0;
        List<ChatRoom> chatRoomList = chatRoomRepository.findChatRoomsByConsumerAndConsumerDeletedOrSellerAndSellerDeleted(
                member,
                0,
                member,
                0
        );
        for(ChatRoom chatRoom : chatRoomList){
            unreadMessages += chatRoomService.calculateUnreadMessageCount(chatRoom, member);
        }
        return unreadMessages;
    }


    @Override
    public List<ChatMessageDto> getAllMessages(int roomId, Member member) {
        ModelMapper modelMapper = new ModelMapper();

        Optional<ChatRoom> chatRoom = chatRoomRepository.findById(roomId); // 후에 null 체크 필요
        Member targetUser = chatRoom.get().getMemberOpponent(member);
        Boolean isDisable =isTargerUserDisable(targetUser);

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


        List<ChatMessage> chatMessageEntitys = getRoomMessages(roomId, member);
        // EntityList -> DTOList
        List<ChatMessageDto> chatMessageDtos = new ArrayList<>();
        for (ChatMessage chatMessageEntity : chatMessageEntitys) {
            ChatMessageUserDto chatMessageUserDto = modelMapper.map(chatMessageEntity.getSender(), ChatMessageUserDto.class);
            if(isDisable){ // 상대방이 유저제재 또는 탈퇴일 경우
                if(chatMessageUserDto.getUserId() == targetUser.getId()){
                    chatMessageUserDto.setNickName("이름없음");
                    chatMessageUserDto.setProfileImgPath(null);
                }
            }
            ChatMessageDto chatMessageDto = new ChatMessageDto();
            chatMessageDto.setRoomId(chatMessageEntity.getRoomId());
            chatMessageDto.setChatMessageUserDto(chatMessageUserDto); // 보내는이 정보
            chatMessageDto.setMessageDate(chatMessageEntity.getMsgDate());
            chatMessageDto.setMessage(chatMessageEntity.getMessage());
            chatMessageDto.setMessage_type(chatMessageEntity.getMsgType());
            chatMessageDto.setMessageStatus(chatMessageEntity.getMsgStatus());
            chatMessageDtos.add(chatMessageDto);
        }

        return chatMessageDtos;
    }


    // ChatMessage Save
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
        Optional<Product> product = productRepository.findById(sendMessage.getProductId());
        ChatMessage savedMessage = null;

        // 채팅방 찾기
        ChatRoom chatRoom =chatRoomRepository.findChatRoomPSR(sendMessage.getProductId(), sendMessage.getSenderId(), sendMessage.getReceiverId());
        LocalDateTime entranceDate = LocalDateTime.now();
        if(chatRoom != null){ // 채팅방 존재 할 경우

            // 레디스 채팅방에 사람 있는 지 확인 -> 있으면 읽음상태로, 없으면 읽지않음상태로 저장
            Optional<RedisChatRoom> redisChatRoom = redisChatRoomService.findByRoomId(chatRoom.getRoomId());
            if(redisChatRoom.isPresent()){
                if(redisChatRoom.get().isSomeoneInChatRoom(String.valueOf(sendMessage.getReceiverId()))){ // 상대방이 채팅방에 들어와있는 경우
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

        }else {
            ChatRoom newChatRoom = ChatRoom.builder()
                    .consumer(sender)
                    .seller(receiver)
                    .product(product.get())
                    .build();
            ChatRoom savedChatroom = chatRoomRepository.save(newChatRoom);
            ArrayList<StompReceivedMessage> receivedMessages = new ArrayList<>();


            if (sendMessage.getMessageType() == 1) { // 채팅으로 거래시 최초로 파일을 올리는 경우
                ArrayList<ChatMessage> chatMessages = createMessagesFromFileListAndChatRoom(sendMessage.getMessage(), savedChatroom, sender, receiver, 0);
                receivedMessages = saveImageMessages2Dto(chatMessages, modelMapper);
            } else {
                ChatMessage chatMessage = createMessageFromSenderTextMessage(sendMessage, savedChatroom, sender, receiver, UNREAD, 0);
                StompReceivedMessage stompReceivedMessage = saveTextMessage2Dto(chatMessage, modelMapper);
                logger.info("[/MESSAGE] save the message : {}}", chatMessage.toString());
                this.template.convertAndSend("/topic/chat/" + sendMessage.getReceiverId(), stompReceivedMessage); // 상대방에게 채팅 메시지를 보낸다
//                logger.info("[/MESSAGE] [SEND] /topic/chat/{}, messages : {}", sendMessage.getReceiverId(), responseMessage);
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
    // 채팅방 별 유저 채팅방 입장 시간 기준으로 읽지 않은 총 메시지의 개수를 계산
    public List<ChatMessage> getRoomMessages(int roomId, Member member){
        ChatRoom chatRoom =chatRoomRepository.getOne(roomId);
        LocalDateTime usersEntranceDate = chatRoom.getUsersEntranceDate(member); // 판매자인지 소비자인지 찾고 판매자이면 판매자의입장시간, 소비자면 소비자의입장시간
        LocalDateTime usersRoomDeletedDate = chatRoom.getUserleaveDate(member);
        logger.info("{} 번 유저 가 요청들어왔음", member.getId());
        logger.info("유저의 입장일 : {}, 유저의 채팅방 삭제(나간)일 : {}",usersEntranceDate, usersRoomDeletedDate);

        if (usersEntranceDate == null & usersRoomDeletedDate == null){ // 유저 입장 X , 나가기 X
            List<ChatMessage> chatMessageList = chatMessageRepository.findChatMessagesByRoomIdOrderByMsgDate(roomId);
            chatMessageList.forEach(e -> logger.info("message date : {}", e.getMsgDate()));
            logger.info("유저 입장 X, 나가기 X, 채팅방 생성일 : {}",chatRoom.getCreated());
            return chatMessageList;

        }else if(usersEntranceDate == null & usersRoomDeletedDate != null){ // 유저 입장 X , 나가기 O
            List<ChatMessage> chatMessageList = chatMessageRepository.findChatMessagesByRoomIdAndMsgDateIsAfterOrderByMsgDateAsc(roomId, usersRoomDeletedDate);
            chatMessageList.forEach(e -> logger.info("message date : {}", e.getMsgDate()));
            logger.info("유저 입장 X, 나가기 O : {}, 채팅방 생성일 : {}",usersRoomDeletedDate, chatRoom.getCreated());
            return chatMessageList;

        }else if(usersEntranceDate != null & usersRoomDeletedDate == null){ // 입장 O, 채팅방 나감 X

            List<ChatMessage> chatMessageList = chatMessageRepository.findChatMessagesByRoomIdOrderByMsgDate(roomId);
            return chatMessageList;

        }else if(usersEntranceDate != null & usersRoomDeletedDate != null){ // 입장도 했고 채팅방도 나간경우

            if( usersRoomDeletedDate.isAfter(usersEntranceDate)){ // 채팅방 나간날이 사용자 입장날보다 빠른경우
                List<ChatMessage> chatMessageList = chatMessageRepository.findChatMessagesByRoomIdAndMsgDateIsAfterOrderByMsgDateAsc(roomId, usersRoomDeletedDate);
                chatMessageList.forEach(e -> logger.info("message date : {}", e.getMsgDate()));
                logger.info("입장일(나중), 채팅방(최신)");
                logger.info("유저 입장일 : {}, 유저 채팅방삭제일 : {}", usersEntranceDate, usersRoomDeletedDate);
                return chatMessageList;
            }else if( usersEntranceDate.isAfter(usersRoomDeletedDate)){
                List<ChatMessage> chatMessageList = chatMessageRepository.findChatMessagesByRoomIdAndMsgDateIsAfterOrderByMsgDateAsc(roomId, usersRoomDeletedDate);
                chatMessageList.forEach(e -> logger.info("message date : {}", e.getMsgDate()));
                logger.info("입장일(최신), 채팅방(나중)");
                logger.info("유저 입장일 : {}, 유저 채팅방삭제일 : {}", usersEntranceDate, usersRoomDeletedDate);
                return chatMessageList;
            }
        }
        return null;
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
    public void sendMessagesToRoom(int roomId, StompReceivedMessage responseMessage){
        this.template.convertAndSend("/topic/room/" + roomId, responseMessage); // 룸으로 메시지 전달
    }

    public void sendRoomInfoMsgToSender(int roomId, String sessionId){
        // 모든 메시지 다 저장한 후에 채팅방 정보를 준다.
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);
        StompRoomInfo stompRoomInfo = new StompRoomInfo(String.valueOf(roomId));
        this.template.convertAndSendToUser(sessionId,"/queue/room/event", stompRoomInfo, headerAccessor.getMessageHeaders());
    }

    // 상대방이 탈퇴 또는 유저제재일 경우를 체크함.
    public boolean isTargerUserDisable(Member targetMember){
        if(targetMember.getIsWithdrawn() == 1 || targetMember.getIsEnabled() == 1){ // null 체크필요
            return true;
        }
        return false;
    }
}
