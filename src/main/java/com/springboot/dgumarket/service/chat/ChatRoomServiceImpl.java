package com.springboot.dgumarket.service.chat;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.springboot.dgumarket.dto.chat.*;
import com.springboot.dgumarket.model.chat.ChatMessage;
import com.springboot.dgumarket.model.chat.ChatRoom;
import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.model.product.Product;
import com.springboot.dgumarket.model.product.ProductReview;
import com.springboot.dgumarket.repository.chat.ChatMessageRepository;
import com.springboot.dgumarket.repository.chat.ChatRoomRepository;
import com.springboot.dgumarket.repository.member.MemberRepository;
import com.springboot.dgumarket.repository.product.ProductRepository;
import com.springboot.dgumarket.repository.product.ProductReviewRepository;
import com.springboot.dgumarket.service.block.UserBlockService;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ChatRoomServiceImpl implements ChatRoomService{
    private static Logger logger = LoggerFactory.getLogger(ChatRoomServiceImpl.class);

    private static final int PRODUCT_STATUS_ETC = 0;
    private static final int PRODUCT_STATUS_CONSUMER = 1;
    private static final int PRODUCT_STATUS_SELLER = 2;
    private static final int PRODUCT_STATUS_PRODUCT_DELETE = 3;


    private static final int SOLD_BY_ANOTHER_ROOM = 2;
    private static final int SOLD = 1;
    private static final int SOLDNOT = 0;
    private static final int YES = 1;
    private static final int NO = 0;


    @Autowired
    ChatRoomRepository chatRoomRepository;

    @Autowired
    ChatMessageRepository chatMessageRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ProductReviewRepository productReviewRepository;

    @Autowired
    UserBlockService userBlockService;

    // 전체 채팅방 목록들 가져오기
    @Override
    public List<ChatRoomListDto> findAllRoomsByUserId(int userId) {
        ModelMapper modelMapper = new ModelMapper();
        Member member = memberRepository.findById(userId);

        PropertyMap<ChatRoom, ChatRoomListDto> chatRoomChatRoomListDtoPropertyMap = new PropertyMap<ChatRoom, ChatRoomListDto>() {
            @Override
            public void configure() {
                map().setRoomId(source.getRoomId());
            }
        };


        // member entity -> member dto
        PropertyMap<Member, ChatMessageUserDto> chatMessageUserDtoPropertyMap = new PropertyMap<Member, ChatMessageUserDto>() {
            @Override
            protected void configure() {
                map().setUserId(source.getId());
                map().setNickName(source.getNickName());
            }
        };

        // product entity -> product dto
        PropertyMap<Product, ChatRoomProductDto> chatRoomProductDtoPropertyMap = new PropertyMap<Product, ChatRoomProductDto>() {
            @Override
            protected void configure() {
                map().setProduct_id(source.getId());
                map().setProductImgPath(source.getImgDirectory());
                map().setProduct_deleted(source.getProductStatus());
            }
        };


        // message entity -> recentMessage dto
        PropertyMap<ChatMessage, ChatRoomRecentMessageDto> chatRoomRecentMessageDtoPropertyMap = new PropertyMap<ChatMessage, ChatRoomRecentMessageDto>() {
            @Override
            protected void configure() {
                map().setMessage(source.getMessage());
                map().setMessage_date(source.getMsgDate());
                map().setMessage_type(source.getMsgType());
            }
        };

        modelMapper.addMappings(chatMessageUserDtoPropertyMap);
        modelMapper.addMappings(chatRoomRecentMessageDtoPropertyMap);
        modelMapper.addMappings(chatRoomProductDtoPropertyMap);
        modelMapper.addMappings(chatRoomChatRoomListDtoPropertyMap);


        // 유저와 관련된 모든 채팅방 가져온다.
        List<ChatRoom> chatRoomList = chatRoomRepository.findChatRoomsByConsumerAndConsumerDeletedOrSellerAndSellerDeleted(
                member,
                0,
                member,
                0
        );


        List<ChatRoomListDto> chatRoomListDtos = new ArrayList<>();

        for (ChatRoom chatRoom : chatRoomList){
            ChatRoomListDto chatRoomListDto = new ChatRoomListDto();

            // 읽지 않음 메시지 개수
            Long unreadMessageCount = calculateUnreadMessageCount(chatRoom, member);

            // 최근메시지
            ChatMessage recentMessage = chatMessageRepository.findFirstByRoomIdOrderByIdDesc(chatRoom.getRoomId());

            // 상대방유저
            Member memberOpponent = chatRoom.getMemberOpponent(member);

            chatRoomListDto.setRoomId(chatRoom.getRoomId());
            chatRoomListDto.setChatRoomRecentMessageDto(modelMapper.map(recentMessage, ChatRoomRecentMessageDto.class));
            chatRoomListDto.setChatRoomProductDto(modelMapper.map(chatRoom.getProduct(), ChatRoomProductDto.class));
            chatRoomListDto.setChatMessageUserDto(modelMapper.map(memberOpponent, ChatMessageUserDto.class));
            chatRoomListDto.setUnreadMessageCount(unreadMessageCount);
            LocalDateTime localDateTime = chatRoomListDto.getChatRoomRecentMessageDto().getMessage_date();
            chatRoomListDto.getChatRoomRecentMessageDto().setMessage_date(localDateTime);
            chatRoomListDto.setBlock(member.IsBlock(memberOpponent));
            chatRoomListDtos.add(chatRoomListDto);
        }

        return chatRoomListDtos;
    }



    // 채팅방 존재유무 체크 ( 사용자가 개별물건화면에서 거래하기 클릭 시 )
    @Override
    public ChatRoomCheckExistedDto findChatRoomByProductSellerConsumer(int productId, int sellerId, int consumerId) {


        ChatRoom chatRoom = chatRoomRepository.findChatRoomsByProductIdAndSellerIdAndConsumerId(productId, sellerId, consumerId);
        ChatRoomCheckExistedDto chatRoomCheckExistedDto = new ChatRoomCheckExistedDto();
        if(chatRoom == null){
            chatRoomCheckExistedDto.setRoom_existed(false);
        }else{
            chatRoomCheckExistedDto.setRoom_existed(true);
            chatRoomCheckExistedDto.setRoom_id(chatRoom.getRoomId());
        }
        return chatRoomCheckExistedDto;
    }


    // 채팅방 상단 Section 물건 정보 보여주기 by Product 1.13
    @Override
    public ChatRoomSectionProductDto findRoomProductSectionByProduct(int productId, int userId) {
        Optional<Product> product = productRepository.findById(productId);

        if (product.get().getProductStatus() == 1){ // 삭제되었을 경우

            return product.map(value -> ChatRoomSectionProductDto.builder() // 삭제상태값만 내려줌
                    .transaction_status_id(4)
                    .build()).orElse(null);
        }

        return product.map(value -> ChatRoomSectionProductDto.builder()
                .product_id(value.getId())
                .product_title(value.getTitle())
                .product_img_path(value.getImgDirectory())
                .product_price(value.getPrice())
                .transaction_status_id(value.getTransactionStatusId())
                .build()).orElse(null);
    }

    // 채팅방 물건 거래완료로 바꾸기
    @Override
    public boolean changeRoomTransactionStatus(int roomId, int status) {
        ChatRoom chatRoom = chatRoomRepository.getOne(roomId);
        if(chatRoom.getProduct().getTransactionStatusId() == 2){ // 이미 거래완료
            return false;
        }
        // 거래완료로 바꾸기
        chatRoom.getProduct().setTransactionStatusId(status);
        // 거래완료 데이터(거래리뷰 null) 추가하기
        ProductReview productReview = ProductReview.builder()
                .consumer(chatRoom.getConsumer())
                .seller(chatRoom.getSeller())
                .product(chatRoom.getProduct())
                .chatRoom(chatRoom)
                .build();
        productReviewRepository.save(productReview);
        return true;
    }

    // 채팅방 나가기
    @Override
    @Transactional
    public void leaveChatRoom(int roomId, int userId) {
        ChatRoom chatRoom = chatRoomRepository.getOne(roomId);
        if(chatRoom != null){
            chatRoom.leave(userId);
        }
    }

    // 채팅방 상태 가져오기
    @Override
    public ChatRoomStatusDto getChatRoomStatus(int roomId, int userId) {
        Member member = memberRepository.findById(userId); // 본인
        ChatRoom chatRoom = chatRoomRepository.getOne(roomId);
        Optional<ProductReview> productReview = productReviewRepository.findByChatRoom(chatRoom);

        // 물건삭제되었을 경우
        if( chatRoom.getProduct().getProductStatus() == 1){
            return ChatRoomStatusDto.builder()
                    .productStatus(PRODUCT_STATUS_PRODUCT_DELETE) // 3
                    .build();
        }

        if(productReview.isPresent()){ // 판매자가 판매를 하였을 경우

            if ( productReview.get().getConsumer() == member){ // 구매자
                ChatRoomStatusDto chatRoomStatusDto = ChatRoomStatusDto.builder()
                        .productStatus(PRODUCT_STATUS_CONSUMER)
                        .transactionStatus(SOLD)
                        .build();
                if(productReview.get().getReviewMessage() == null){
                    chatRoomStatusDto.setIsReviewUpload(NO);
                }else {
                    chatRoomStatusDto.setIsReviewUpload(YES);
                }
                return chatRoomStatusDto;

            }else if( productReview.get().getSeller() == member){ // 판매자


                String reviewerNickname  = productReview.get().getConsumer().getNickName();
                ChatRoomStatusDto chatRoomStatusDto = ChatRoomStatusDto.builder()
                        .productStatus(PRODUCT_STATUS_SELLER)
                        .transactionStatus(SOLD)
                        .reviewer_nickname(reviewerNickname).build();

                if(productReview.get().getReviewMessage() == null){ // 거래완료 & 구매자 후기 아직 안남김
                    chatRoomStatusDto.setIsReviewUpload(NO);
                }else {
                    chatRoomStatusDto.setIsReviewUpload(YES); // 구매후기 있고
                }

                return chatRoomStatusDto;
            }else { // 아무도 아닌 사람
                return ChatRoomStatusDto.builder()
                        .productStatus(PRODUCT_STATUS_ETC).build();
            }
        }else{ // 판매자가 구매완료 버튼 누르지 않은 경우 ( 모두에게 공평 ) + 구매버튼을 눌러도 해당 거래완료한 해당 채팅방이 아닐경우




            if(chatRoom.getSeller() == member){ // 물건 올린 사람

                if(chatRoom.getProduct().getTransactionStatusId() == 2){ // 이미 다른 곳에서 판매자가 거래완료 함
                    return ChatRoomStatusDto.builder()
                            .productStatus(PRODUCT_STATUS_SELLER)
                            .transactionStatus(SOLD_BY_ANOTHER_ROOM).build();
                }

                return ChatRoomStatusDto.builder()
                        .productStatus(PRODUCT_STATUS_SELLER)
                        .transactionStatus(SOLDNOT).build();
            }else {
                return ChatRoomStatusDto.builder()
                        .productStatus(PRODUCT_STATUS_ETC).build();
            }
        }
    }




    // 채팅방 별 유저 채팅방 입장 시간 기준으로 읽지 않은 총 메시지의 개수 계산
    public Long calculateUnreadMessageCount(ChatRoom chatRoom, Member member){

        logger.info("{}번 채팅방 의 {} 번 유저", chatRoom.getRoomId(), member.getId());
        LocalDateTime usersEntranceDate = chatRoom.getUsersEntranceDate(member); // 판매자인지 소비자인지 찾고 판매자이면 판매자의입장시간, 소비자면 소비자의입장시간
        LocalDateTime usersRoomDeletedDate = chatRoom.getUserleaveDate(member);

        if (usersEntranceDate != null && usersRoomDeletedDate == null){ // 입장만 하고 채팅방을 나가지 않은 경우
            Long countUserUnreadMsgAfterEntranceDate = chatMessageRepository.countAllByRoomIdAndMsgDateIsAfterAndReceiverAndMsgStatus(chatRoom.getRoomId(), usersEntranceDate, member, 0);
            logger.info("1채팅방삭제일 : {} , 유저입장일 : {}, 읽지않은메시지개수 : {}", usersEntranceDate, usersEntranceDate, countUserUnreadMsgAfterEntranceDate);
            return countUserUnreadMsgAfterEntranceDate;
        }else if(usersEntranceDate == null && usersRoomDeletedDate == null){ // 입장도 하지않고 채팅방 나가지도 않은 경우
            Long countUserUnreadMsgAfterEntranceDate = chatMessageRepository.countAllByRoomIdAndReceiverAndMsgStatus(chatRoom.getRoomId(), member, 0);
            logger.info("2채팅방삭제일 : {} , 유저입장일 : {}, 읽지않은메시지개수 : {}", usersEntranceDate, usersEntranceDate, countUserUnreadMsgAfterEntranceDate);
            return countUserUnreadMsgAfterEntranceDate;
        }else if(usersEntranceDate == null && usersRoomDeletedDate != null){ // 입장은 하지않고 채팅방만 나간경우

            Long countUserUnreadMsgAfterEntranceDate = chatMessageRepository.countAllByRoomIdAndMsgDateIsAfterAndReceiverAndMsgStatus(chatRoom.getRoomId(), usersRoomDeletedDate, member, 0);
            logger.info("3채팅방삭제일 : {} , 유저입장일 : {}, 읽지않은메시지개수 : {}", usersEntranceDate, usersEntranceDate, countUserUnreadMsgAfterEntranceDate);
            return countUserUnreadMsgAfterEntranceDate;
        }else { // 입장도 했고 채팅방도 나간경우

            if( usersRoomDeletedDate.isAfter(usersEntranceDate)){ // 채팅방 나간날이 사용자 입장날보다 빠른경우
                Long countMessageAfterUserDeletedRoom = chatMessageRepository.countAllByRoomIdAndMsgDateIsAfterAndReceiverAndMsgStatus(chatRoom.getRoomId(), usersRoomDeletedDate, member, 0);
                logger.info("4채팅방삭제일 : {} , 유저입장일 : {}, 읽지않은메시지개수 : {}", usersEntranceDate, usersEntranceDate, countMessageAfterUserDeletedRoom);
                return countMessageAfterUserDeletedRoom;
            }else {
                Long countUserUnreadMsgAfterEntranceDate = chatMessageRepository.countAllByRoomIdAndMsgDateIsAfterAndReceiverAndMsgStatus(chatRoom.getRoomId(), usersEntranceDate, member, 0);
                logger.info("5채팅방삭제일 : {} , 유저입장일 : {}, 읽지않은메시지개수 : {}", usersEntranceDate, usersEntranceDate, countUserUnreadMsgAfterEntranceDate);
                return countUserUnreadMsgAfterEntranceDate;
            }
        }
    }




}
