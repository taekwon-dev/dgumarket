package com.springboot.dgumarket.service.chat;

import com.amazonaws.services.devicefarm.model.transform.OfferingJsonUnmarshaller;
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
import org.modelmapper.AbstractConverter;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.spi.MappingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
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
    SimpMessagingTemplate template;

    @Autowired
    ChatRoomRepository chatRoomRepository;

    @Autowired
    ChatMessageRepository chatMessageRepository;

    @Autowired
    MemberRepository memberRepository;


    @Autowired
    RedisChatRoomService redisChatRoomService;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ChatRoomService chatRoomService;

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

        logger.info("sendMessage : {}",sendMessage.toString());
        // 채팅방 찾기
        ChatRoom chatRoom =chatRoomRepository.findChatRoomPSR(sendMessage.getProductId(), sendMessage.getSenderId(), sendMessage.getReceiverId());
        if(chatRoom != null){ // 채팅방 존재 할 경우

            // 레디스 채팅방에 사람 있는 지 확인 -> 있으면 읽음상태로, 없으면 읽지않음상태로 저장
            Optional<RedisChatRoom> redisChatRoom = redisChatRoomService.findByRoomId(chatRoom.getRoomId());
            if(redisChatRoom.isPresent()){

                if(redisChatRoom.get().isSomeoneInChatRoom(String.valueOf(sendMessage.getReceiverId()))){ // 상대방이 채팅방에 들어와있는 경우
                    // 채팅방 나감 유무 1과 0은 레디스의 채팅방 나감유무와 별개이다


                    logger.info("[/MESSAGE] someone is in chat room in (redis)chatroom");
                    LocalDateTime entranceDate = LocalDateTime.now();

                    // 채팅메시지를 저장시 '읽음'상태로 저장한다.
                    savedMessage = chatMessageRepository.save(sendMessage.toEntityWith(
                            READ,
                            chatRoom.getRoomId(),
                            chatRoom.getProduct(),
                            receiver,
                            sender));

                    // 매핑
                    responseMessage.setChatMessageUserDto(modelMapper.map(savedMessage.getSender(), ChatMessageUserDto.class)); // 보내는이 정보
                    responseMessage.setChatRoomProductDto(modelMapper.map(chatRoom.getProduct(), ChatRoomProductDto.class));
                    responseMessage.setRoomId(savedMessage.getRoomId());
                    responseMessage.setMessageDate(savedMessage.getMsgDate());
                    responseMessage.setMessage(savedMessage.getMessage());
                    responseMessage.setMessage_type(savedMessage.getMsgType());
                    responseMessage.setMessageStatus(savedMessage.getMsgStatus());


                    // 만약 상대방 나갔을 경우 나가기1 -> 나가기0
                    chatRoom.changeExitToJoin(sendMessage.getReceiverId());

                    // 로직 추가, 보내기 전에 만약 내가 해당 채팅방에서 나가기 상태(1) -> 나가지않은상태(0)으로 바꾸어 준다
                    chatRoom.leave2enterForFirstMessage(sender, entranceDate);
                    this.template.convertAndSend("/topic/room/" + responseMessage.getRoomId(), responseMessage); // 룸에 있는 사람에게 전달

                    logger.info("[/MESSAGE], [SEND] /topic/room/{}, {}", responseMessage.getRoomId(), responseMessage);
                    logger.info("[/MESSAGE] save chat message (status 0 -> 1) : {}, 읽음 상태 : {}", savedMessage, savedMessage.getMsgStatus());
                }else {
                    logger.info("[/MESSAGE] someone isn't in chat room in (redis)chatroom");

                    LocalDateTime entranceDate = LocalDateTime.now(); // 입장일

                    savedMessage = chatMessageRepository.save(sendMessage.toEntityWith(
                            UNREAD,
                            chatRoom.getRoomId(),
                            chatRoom.getProduct(),
                            receiver,
                            sender));

                    logger.info("[/MESSAGE] save chat message (status 0 -> 0) : {}, 읽음 상태 : {}", savedMessage, savedMessage.getMsgStatus());

                    // 매핑하기
                    responseMessage.setChatMessageUserDto(modelMapper.map(savedMessage.getSender(), ChatMessageUserDto.class)); // 보내는이 정보
                    responseMessage.setChatRoomProductDto(modelMapper.map(chatRoom.getProduct(), ChatRoomProductDto.class));

                    responseMessage.setRoomId(savedMessage.getRoomId());
                    responseMessage.setMessageDate(savedMessage.getMsgDate());
                    responseMessage.setMessage(savedMessage.getMessage());
                    responseMessage.setMessage_type(savedMessage.getMsgType());
                    responseMessage.setMessageStatus(savedMessage.getMsgStatus());


                    // 만약 상대방 나갔을 경우 나가기1 -> 나가기0
                    chatRoom.changeExitToJoin(sendMessage.getReceiverId());
                    // 로직 추가, 보내기 전에 만약 내가 해당 채팅방에서 나가기 상태(1) -> 나가지않은상태(0)으로 바꾸어 준다
                    chatRoom.leave2enterForFirstMessage(sender, entranceDate);

                    this.template.convertAndSend("/topic/room/" + responseMessage.getRoomId(), responseMessage); // 룸으로 메시지 전달
                    this.template.convertAndSend("/topic/chat/" + sendMessage.getReceiverId(), responseMessage); // 상대방에게 메시지 전달
                    logger.info("[/MESSAGE] [SEND] /topic/room/{}, {}", responseMessage.getRoomId(), responseMessage);
                    logger.info("[/MESSAGE] [SEND] /topic/chat/{}, {}", sendMessage.getReceiverId(), responseMessage);
                }

            }

        }else {

            /**
             * [채팅방(mysql)이 존재하지 않을 경우]
             *              * TODO: [채팅방이 존재하지 않을 경우] 의 상황 구체적으로 정의하고 코드작성 필요
             *              * 20/11/26 생각
             *              * 사용자로 부터 받은 채팅메시지로 새로운 채팅방을 만든다. chatService.newCreateChatRoom(message)
             *              * 새롭게 만든 채팅방의 방 번호 와 채팅메시지를 통해 채팅 메시지를 저장한다. chatMessageRepo.save(chatMessage.toEntitityWith(chatmessage, roomid, UNREAD)
             *              * 사용자를 채팅방에 가입시킨다.
             *              * ( 사용자는 어떻게 구독을 할 것인가. , 새로운 액션을 보내자,
             *              *      [v1 // 여기서 구조가 다르다. , 기존 만들어진 채팅방(이미 누군가 대화하여 생긴 채팅방)에 들어갈 떄 서버에 채팅방 가입하도록 하는 메시지를 전달하는 구조 ]
             *      [v2 사용자로부터 메시지를 받자마자 채팅방에 가입시킨다. ]
             *
             *      4/5 추가
             *      [채팅으로 거래하기 시, 여러개의 이미지파일을 업로드를 통해 채팅방을 개설하는 경우]
             */
            logger.info("[/MESSAGE] room is not existed, room : {}", chatRoom);

            ChatRoom newChatRoom = ChatRoom.builder()
                    .consumer(sender)
                    .seller(receiver)
                    .product(product.get())
                    .build();
            ChatRoom savedChatroom = chatRoomRepository.save(newChatRoom);
            List<StompReceivedMessage> chatMessageDtoList = new ArrayList<>();
            if(sendMessage.getMessageType() == 1){ // 채팅으로 거래시 최초로 파일을 올리는 경우
                String[] filelist = sendMessage.getMessage()
                        .replace("[", "")
                        .replace("]", "")
                        .split(", ");
                System.out.println("filelist4.length = " + filelist.length);
                for (int i = 0; i < filelist.length; i++) {

                    ChatMessage chatMessage = chatMessageRepository.save(ChatMessage.builder() // 메시지 저장
                                    .message(filelist[i])
                                    .msgDate(LocalDateTime.now())
                                    .msgStatus(UNREAD)
                                    .msgType(1)
                                    .receiver(sender)
                                    .sender(sender)
                                    .product(product.get())
                                    .roomId(savedChatroom.getRoomId()).build());
                    StompReceivedMessage stompReceivedMessage = new StompReceivedMessage();
                    // 매핑
                    stompReceivedMessage.setChatMessageUserDto(modelMapper.map(chatMessage.getSender(), ChatMessageUserDto.class)); // 보내는이 정보
                    stompReceivedMessage.setChatRoomProductDto(modelMapper.map(product.get(), ChatRoomProductDto.class));
                    stompReceivedMessage.setRoomId(chatMessage.getRoomId());
                    stompReceivedMessage.setMessageDate(chatMessage.getMsgDate());
                    stompReceivedMessage.setMessage(chatMessage.getMessage());
                    stompReceivedMessage.setMessage_type(chatMessage.getMsgType());
                    stompReceivedMessage.setMessageStatus(chatMessage.getMsgStatus());

                    // 전달할 메시지리스트에 추가
                    chatMessageDtoList.add(stompReceivedMessage);
                }
            }else{
                logger.info("[/MESSAGE] create new chat mysql chat room by message - chatroom : {}, message: {}}", newChatRoom.toString(), sendMessage.getMessage());
                savedMessage = chatMessageRepository.save(sendMessage.toEntityWith(UNREAD, savedChatroom.getRoomId(), product.get(), receiver, sender));
                logger.info("[/MESSAGE] save the message : {}}", savedMessage.toString());

                // 매핑
                responseMessage.setChatMessageUserDto(modelMapper.map(savedMessage.getSender(), ChatMessageUserDto.class)); // 보내는이 정보
                responseMessage.setChatRoomProductDto(modelMapper.map(product.get(), ChatRoomProductDto.class));
                responseMessage.setRoomId(savedMessage.getRoomId());
                responseMessage.setMessageDate(savedMessage.getMsgDate());
                responseMessage.setMessage(savedMessage.getMessage());
                responseMessage.setMessage_type(savedMessage.getMsgType());
                responseMessage.setMessageStatus(savedMessage.getMsgStatus());
                this.template.convertAndSend("/topic/chat/" + sendMessage.getReceiverId(), responseMessage); // 상대방에게 채팅 메시지를 보낸다

//                logger.info("[/MESSAGE] [SEND] 룸정보를 줍니다, {}", stompRoomInfo.toString());
//                logger.info("[/MESSAGE] [SEND] /queue/room/event, sessionId : {} message: {}", sessionId, stompRoomInfo);
                logger.info("[/MESSAGE] [SEND] /topic/chat/{}, messages : {}", sendMessage.getReceiverId(), responseMessage);
            }

            // 모든 메시지 다 저장한 후에 채팅방 정보를 준다.
            SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
            headerAccessor.setSessionId(sessionId);
            headerAccessor.setLeaveMutable(true);
            StompRoomInfo stompRoomInfo = new StompRoomInfo(String.valueOf(savedChatroom.getRoomId()));
            this.template.convertAndSendToUser(sessionId,"/queue/room/event", stompRoomInfo, headerAccessor.getMessageHeaders());

            // 상대방에게 메시지를 보냄
            if(sendMessage.getMessageType() == 1){
                chatMessageDtoList.forEach(responseMsg -> this.template.convertAndSend("/topic/chat/" + sendMessage.getReceiverId(), responseMsg));
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

    // 상대방이 탈퇴 또는 유저제재일 경우를 체크함.
    public boolean isTargerUserDisable(Member targetMember){
        if(targetMember.getIsWithdrawn() == 1 || targetMember.getIsEnabled() == 1){ // null 체크필요
            return true;
        }
        return false;
    }
}
