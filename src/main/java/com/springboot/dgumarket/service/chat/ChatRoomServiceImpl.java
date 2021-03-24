package com.springboot.dgumarket.service.chat;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.springboot.dgumarket.controller.chat.UserValidationForChatRoom;
import com.springboot.dgumarket.dto.chat.*;
import com.springboot.dgumarket.exception.CustomControllerExecption;
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
import com.springboot.dgumarket.service.Validation.ValidationService;
import com.springboot.dgumarket.service.block.UserBlockService;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

    @Autowired
    ValidationService validationService;

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
    public void changeRoomTransactionStatus(int userId, int roomId, int status) throws CustomControllerExecption {
        Member member = memberRepository.findById(userId); // 이미 여기서 탈퇴가 걸러진다.
        if(member==null){throw new CustomControllerExecption("존재하지않는 유저는 해당 기능을 이용하실 수 엇습니다.", HttpStatus.BAD_REQUEST);}
        if(member.getIsEnabled()==1){throw new CustomControllerExecption("관리자로부터 이용제재를 받고있는 유저는 해당 기능을 사용하실 수 없습니다.", HttpStatus.BAD_REQUEST);}


        ChatRoom chatRoom = chatRoomRepository.getOne(roomId);
        // 물건 삭제 또는 블라인드 처리되었을 경우 불가
        if(chatRoom.getProduct()==null || chatRoom.getProduct().getProductStatus()==1){throw new CustomControllerExecption("해당 중고물품은 삭제처리되었습니다.", HttpStatus.NOT_FOUND);}
        if(chatRoom.getProduct().getProductStatus()==2){throw new CustomControllerExecption("관리자에 의해 비공개 처리되어 거래완료를 할 수 없습니다.", HttpStatus.BAD_REQUEST);}

        // 채팅방의 물건 거래완료로 바꾸려는 데 채팅방에 있는 대화상대가 내가 차단한 경우라면 불가능
        // 채팅방 상대방이 사용자제재, 탈퇴 유저인지 확인하기
        Member opponentMember = chatRoom.getMemberOpponent(member);
        if(opponentMember.getIsEnabled()==1){throw new CustomControllerExecption("이용제재를 받고 있는 유저와 거래완료를 할 수 없습니다.", HttpStatus.NOT_FOUND);}
        if(opponentMember.getIsWithdrawn()==1 || opponentMember == null){throw new CustomControllerExecption("탈퇴한 유저와 거래완료를 할 수 없습니다.", HttpStatus.BAD_REQUEST);}

        if(chatRoom.getSeller() == member){ // 반드시 채팅방의 판매자가 요청한것이여야함
            if (member.getBlockUsers().contains(chatRoom.getConsumer()) || member.getUserBlockedMe().contains(chatRoom.getConsumer())){ // 서로 차단 중이라면
                throw new CustomControllerExecption("차단된 유저와는 거래완료를 할 수 없습니다.", HttpStatus.FORBIDDEN);
            }
            if(chatRoom.getProduct().getTransactionStatusId() == 2){ // 이미 거래완료
                throw new CustomControllerExecption("이미 거래완료 되어 있는 상태입니다.", HttpStatus.BAD_REQUEST);
            }

            if (chatRoom.getProduct().getTransactionStatusId() == 0){
                // 거래완료로 바꾸기
                chatRoom.getProduct().setTransactionStatusId(status);

                // 거래완료 데이터(거래리뷰 null) 추가하기
                ProductReview productReview = ProductReview.builder()
                        .consumer(chatRoom.getConsumer())
                        .seller(chatRoom.getSeller())
                        .product(chatRoom.getProduct())
                        .createdDate(LocalDateTime.now())
                        .chatRoom(chatRoom)
                        .build();
                productReviewRepository.save(productReview);
            }
        }else{ // 판매자 요청이 아닌 경우
            throw new CustomControllerExecption("잘못된 요청입니다", HttpStatus.BAD_REQUEST);
        }
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
//    @UserValidationForChatRoom
    public ChatRoomStatusDto getChatRoomStatus(int roomId, int userId) {
        Member member = memberRepository.findById(userId); // 본인
        ChatRoom chatRoom = chatRoomRepository.getOne(roomId);
        Optional<ProductReview> productReview = productReviewRepository.findByChatRoom(chatRoom);


        // 물건삭제되었을 경우
        if( chatRoom.getProduct().getProductStatus() == 1){
            return ChatRoomStatusDto.builder()
                    .productStatus(PRODUCT_STATUS_PRODUCT_DELETE) // 3
                    .isWarn(chatRoom.getMemberOpponent(member).checkWarnActive()) // 경고유무
                    .block_status(member.checkBlockStatus(chatRoom.getMemberOpponent(member))) // 차단상태
                    .build();
        }

        if(productReview.isPresent()){ // 판매자가 판매를 하였을 경우

            if ( productReview.get().getConsumer() == member){ // 구매자
                ChatRoomStatusDto chatRoomStatusDto = ChatRoomStatusDto.builder()
                        .productStatus(PRODUCT_STATUS_CONSUMER)
                        .transactionStatus(SOLD)
                        .isWarn(chatRoom.getMemberOpponent(member).checkWarnActive()) // 경고유무
                        .block_status(member.checkBlockStatus(chatRoom.getMemberOpponent(member))) // 차단상태
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
                        .reviewer_nickname(reviewerNickname)
                        .isWarn(chatRoom.getMemberOpponent(member).checkWarnActive()) // 경고유무
                        .block_status(member.checkBlockStatus(chatRoom.getMemberOpponent(member))) // 차단상태
                        .build();

                if(productReview.get().getReviewMessage() == null){ // 거래완료 & 구매자 후기 아직 안남김
                    chatRoomStatusDto.setIsReviewUpload(NO);
                }else {
                    chatRoomStatusDto.setIsReviewUpload(YES); // 구매후기 있고
                }

                return chatRoomStatusDto;
            }else { // 아무도 아닌 사람
                return ChatRoomStatusDto.builder()
                        .productStatus(PRODUCT_STATUS_ETC)
                        .isWarn(chatRoom.getMemberOpponent(member).checkWarnActive()) // 경고유무
                        .block_status(member.checkBlockStatus(chatRoom.getMemberOpponent(member))) // 차단상태
                        .build();
            }
        }else{ // 판매자가 구매완료 버튼 누르지 않은 경우 ( 모두에게 공평 ) + 구매버튼을 눌러도 해당 거래완료한 해당 채팅방이 아닐경우




            if(chatRoom.getSeller() == member){ // 물건 올린 사람

                if(chatRoom.getProduct().getTransactionStatusId() == 2){ // 이미 다른 곳에서 판매자가 거래완료 함
                    return ChatRoomStatusDto.builder()
                            .productStatus(PRODUCT_STATUS_SELLER)
                            .transactionStatus(SOLD_BY_ANOTHER_ROOM)
                            .isWarn(chatRoom.getMemberOpponent(member).checkWarnActive()) // 경고유무
                            .block_status(member.checkBlockStatus(chatRoom.getMemberOpponent(member))) // 차단상태
                            .build();
                }

                return ChatRoomStatusDto.builder()
                        .productStatus(PRODUCT_STATUS_SELLER)
                        .transactionStatus(SOLDNOT)
                        .isWarn(chatRoom.getMemberOpponent(member).checkWarnActive()) // 경고유무
                        .block_status(member.checkBlockStatus(chatRoom.getMemberOpponent(member))) // 차단상태
                        .build();
            }else {
                return ChatRoomStatusDto.builder()
                        .productStatus(PRODUCT_STATUS_ETC)
                        .isWarn(chatRoom.getMemberOpponent(member).checkWarnActive()) // 경고유무
                        .block_status(member.checkBlockStatus(chatRoom.getMemberOpponent(member))) // 차단상태
                        .build();
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


    // 채팅으로거래하기 클릭시 (나가기유무 추가)
    @Override
    public ChatRoomTradeHistoryDto checkChatHistory(int userId, int productId) throws CustomControllerExecption {
        System.out.println("채팅으로거래하기 : 아이디" + userId +" / 물건번호" + productId);
        Optional<Product> product = productRepository.findById(productId);
        product.orElseThrow(()-> new CustomControllerExecption("존재하지 않은 물건입니다.", HttpStatus.NOT_FOUND));
        ChatRoom chatRoom = chatRoomRepository.findChatRoomsByProductIdAndSellerIdAndConsumerId(productId, product.get().getMember().getId(), userId); // 채팅방 있는 지 체크
        if(chatRoom != null){
            logger.info("chatroom : {}", chatRoom.getRoomId());
            Member member = memberRepository.findById(userId);
            ChatRoomTradeHistoryDto chatRoomTradeHistoryDto = ChatRoomTradeHistoryDto.builder()
                    .history_product_id(chatRoom.getProduct().getId())
                    .history_room_id(chatRoom.getRoomId())
                    .isExisted(true).build();
            if(chatRoom.isMine(member)){//seller
                if(chatRoom.getSellerDeleted()==1){ // 채팅방 나갔을 경우
                    chatRoomTradeHistoryDto.setLeave(true);
                }
            }else{ //consumer
                if(chatRoom.getConsumerDeleted()==1) {
                    chatRoomTradeHistoryDto.setLeave(true);
                }
            }
            return chatRoomTradeHistoryDto;
        }
        // (물건삭제, 물건블라인드), 유저제재, 유저탈퇴, 유저차단
        if(product.get().getProductStatus() == 1){throw new CustomControllerExecption("삭제된 중고물품의 경우 채팅거래를 하실 수 없습니다.", HttpStatus.NOT_FOUND);}
        if(product.get().getProductStatus() == 2){throw new CustomControllerExecption("관리자에 의해 비공개 처리된 물건입니다. 채팅거래를 하실 수 없습니다.", HttpStatus.BAD_REQUEST);}
        if(product.get().getMember().getIsWithdrawn()==1){throw new CustomControllerExecption("탈퇴한 유저입니다. 채팅거래를 하실 수 없습니다.", HttpStatus.NOT_FOUND);}
        if(product.get().getMember().getIsEnabled()==1){throw new CustomControllerExecption("관리자로부터 이용제재당한 유저와 채팅거래를 하실 수 없습니다.", HttpStatus.BAD_REQUEST);}
        Member member = memberRepository.findById(userId);
        if(member.getBlockUsers().contains(product.get().getMember())){
            throw new CustomControllerExecption("차단한 유저와는 채팅거래를 할 수 없습니다.", HttpStatus.BAD_REQUEST);
        }

        if(member.getUserBlockedMe().contains(product.get().getMember())) {
            throw new CustomControllerExecption("차단 당한 유저와는 채팅거래를 할 수 없습니다", HttpStatus.BAD_REQUEST);
        }
        return ChatRoomTradeHistoryDto.builder()
                .isExisted(false).build();
    }
}
