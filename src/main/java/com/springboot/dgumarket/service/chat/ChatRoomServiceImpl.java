package com.springboot.dgumarket.service.chat;


import com.springboot.dgumarket.dto.chat.*;
import com.springboot.dgumarket.exception.CustomControllerExecption;
import com.springboot.dgumarket.model.chat.ChatMessage;
import com.springboot.dgumarket.model.chat.ChatRoom;
import com.springboot.dgumarket.model.member.BlockUser;
import com.springboot.dgumarket.model.member.Member;
import com.springboot.dgumarket.model.product.Product;
import com.springboot.dgumarket.model.product.ProductReview;
import com.springboot.dgumarket.repository.chat.ChatMessageRepository;
import com.springboot.dgumarket.repository.chat.ChatRoomRepository;
import com.springboot.dgumarket.repository.member.BlockUserRepository;
import com.springboot.dgumarket.repository.member.MemberRepository;
import com.springboot.dgumarket.repository.product.ProductRepository;
import com.springboot.dgumarket.repository.product.ProductReviewRepository;
import com.springboot.dgumarket.service.Validation.ValidationService;
import com.springboot.dgumarket.service.block.UserBlockService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Slf4j
@Service
public class ChatRoomServiceImpl implements ChatRoomService{

    private static final int PRODUCT_STATUS_ETC = 0;
    private static final int PRODUCT_STATUS_CONSUMER = 1;
    private static final int PRODUCT_STATUS_SELLER = 2;
    private static final int PRODUCT_STATUS_PRODUCT_DELETE = 3;
    private static final int PRODUCT_STATUS_PRODUCT_BLIEND = 4;


    private static final int SOLD_BY_ANOTHER_ROOM = 2;
    private static final int SOLD = 1;
    private static final int SOLDNOT = 0;
    private static final int YES = 1;
    private static final int NO = 0;


    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BlockUserRepository blockUserRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductReviewRepository productReviewRepository;

    @Autowired
    private UserBlockService userBlockService;

    @Autowired
    private ValidationService validationService;


    // 전체 채팅방 목록들 가져오기

    // 채팅 상대방 유저가 삭제되지 않은 경우

    // 채팅 상대방 유저가 삭제된 경우
    // 1. 채팅 상대방 유저 (= 구매자)
    // 2. 채팅 상대방 유저 (= 판매자), 상품 정보 삭제
    /** [TESTED] */
    @Override
    public List<ChatRoomListDto> findAllRoomsByUserId(int userId) {

        // 로그인 유저
        Member loginUser = memberRepository.findById(userId);

        // ModelMapper 객체 초기화
        ModelMapper modelMapper = new ModelMapper();
        PropertyMap<ChatRoom, ChatRoomListDto> chatRoomChatRoomListDtoPropertyMap = new PropertyMap<ChatRoom, ChatRoomListDto>() {
            @Override
            public void configure() {
                map().setRoomId(source.getRoomId());
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

        // mapper 객체에 각 Entity -> DTO 방향으로 지정한 매핑 방식 등록

        // 채팅방 목록에 사용되는 유저 정보 객체
        modelMapper.addMappings(chatMessageUserDtoPropertyMap);

        // 채팅방 목록에 사용되는 채팅 메시지 객체 (= 가장 마지막 메시지)
        modelMapper.addMappings(chatRoomRecentMessageDtoPropertyMap);

        // 채팅방 목록에 사용되는 상품 정보 객체
        modelMapper.addMappings(chatRoomProductDtoPropertyMap);

        // 채팅방 목록에 사용되는 채팅방 고유 ID
        modelMapper.addMappings(chatRoomChatRoomListDtoPropertyMap);


        // 로그인 유저와 관련된 모든 채팅방 가져온다.
        // 채팅방 테이블에서, 판매자 또는 구매자 = 로그인 유저인 조건을 두고 조회
        List<ChatRoom> chatRoomList = chatRoomRepository.findChatRoomsByConsumerAndConsumerDeletedOrSellerAndSellerDeleted(
                loginUser, // 채팅방에서 로그인 유저가 구매자로 있는 경우
                0, // 로그인 유저 (= 구매자 입장)가 삭제하지 않은 채팅방 대상
                loginUser, // 채팅방에서 로그인 유저가 판매자로 있는 경우
                0 // 로그인 유저 (= 판매자 입장)가 삭제하지 않은 채팅방 대상
        );


        List<ChatRoomListDto> chatRoomListDtos = new ArrayList<>();

        // 채팅방 목록 조회 -> 각 채팅방 목록에 렌더링 할 데이터 조회
        // 1. 안 읽은 메시지 수
        // 2. 마지막으로 주고 받은 메시지
        // 3. 채팅 상대방 유저 정보
        // 4. 채팅에 연결된 상품 정보


        for (ChatRoom chatRoom : chatRoomList) {
            ChatRoomListDto chatRoomListDto = new ChatRoomListDto();

            // 로그인 유저가 해당 채팅방에서 안 읽은 메시지 수 조회
            Long unreadMessageCount = calculateUnreadMessageCount(chatRoom, loginUser);

            // 해당 채팅방에서 마지막으로 주고 받은 메시지 정보
            // 채팅 상대방 유저가 삭제 되어도 방 정보를 기준으로 마지막 메시지를 조회하므로, 영향이 없다.
            ChatMessage recentMessage = chatMessageRepository.findFirstByRoomIdOrderByIdDesc(chatRoom.getRoomId());

            // 상대방 유저
            // NPE 대상 (= 채팅방 상대 유저가 삭제된 경우, NULL 값)
            Member opponentUser = chatRoom.getMemberOpponent(loginUser);

            // 해당 채팅의 상품 정보
            // NPE 대상 (= 채팅방 상대 유저가 판매자 + 삭제된 경우, NULL 값)
            Product chatProduct = chatRoom.getProduct();

            // 아래 조건문은 총 세 가지 경우의 수로 나뉜다.
            // 1. 채팅 상대방 유저가 NULL && 채팅과 연결된 상품 정보가 NULL (= 채팅 상대방이 판매자인 경우)
            // 2. 채팅 상대방 유저가 NULL && 채팅과 연결된 상품 정보가 ~NULL (= 채팅 상대방이 구매자인 경우)
            // 3. 채팅 상대방 유저가 ~NULL (채팅 상대방이 삭제되지 않은 경우)
            // (참고 - 채팅 상대방 유저가 ~NULL인 상황에서 상품 정보가 NULL 되는 경우는 없다.)


            if (opponentUser == null && chatProduct == null) {
                // 1. 채팅 상대방 유저가 NULL && 채팅과 연결된 상품 정보가 NULL (= 채팅 상대방이 판매자인 경우)

                /**
                 *  {
                 *    "roomId": 106,
                 *    "chatRoomRecentMessageDto":
                 *      {
                 *        "message_type": 0,
                 *        "message_date": "2021-01-15T18:07:56",
                 *        "message": "안뇽"
                 *      },
                 *    "unreadMessageCount": 0
                 *   }
                 * */

                // [roomId] : 채팅방 고유 ID 지정 (가능)
                chatRoomListDto.setRoomId(chatRoom.getRoomId());

                // [chatRoomRecentMessageDto] : 채팅방에서 주고 받은 마지막 메시지 (via 채팅방 고유 ID) (가능)
                chatRoomListDto.setChatRoomRecentMessageDto(modelMapper.map(recentMessage, ChatRoomRecentMessageDto.class));

                // [chatRoomRecentMessageDto] : 마지막으로 주고 받은 메시지의 시간 정보 (가능)
                LocalDateTime localDateTime = chatRoomListDto.getChatRoomRecentMessageDto().getMessage_date();
                chatRoomListDto.getChatRoomRecentMessageDto().setMessage_date(localDateTime);

                // [unreadMessageCount] : 로그인 유저가 안 읽은 메시지 수는 (로그인 유저 & 채팅방)을 통해 조회하므로 (가능)
                chatRoomListDto.setUnreadMessageCount(unreadMessageCount);


            } else if (opponentUser == null && chatProduct != null) {
                // 2. 채팅 상대방 유저가 NULL && 채팅과 연결된 상품 정보가 ~NULL (= 채팅 상대방이 구매자인 경우)

                /**
                 *  {
                 *    "roomId": 106,
                 *    "chatRoomRecentMessageDto":
                 *      {
                 *        "message_type": 0,
                 *        "message_date": "2021-01-15T18:07:56",
                 *        "message": "안뇽"
                 *      },
                 *    "unreadMessageCount": 0,
                 *    "chatRoomProductDto":
                 *      {
                 *        "product_id": 5,
                 *        "product_deleted": 0,
                 *        "productImgPath": "/imgs/slideshow_sample.jpg"
                 *      }
                 *   }
                 * */

                // [roomId] : 채팅방 고유 ID 지정 (가능)
                chatRoomListDto.setRoomId(chatRoom.getRoomId());

                // [chatRoomRecentMessageDto] : 채팅방에서 주고 받은 마지막 메시지 (via 채팅방 고유 ID) (가능)
                chatRoomListDto.setChatRoomRecentMessageDto(modelMapper.map(recentMessage, ChatRoomRecentMessageDto.class));

                // [chatRoomRecentMessageDto] : 마지막으로 주고 받은 메시지의 시간 정보 (가능)
                LocalDateTime localDateTime = chatRoomListDto.getChatRoomRecentMessageDto().getMessage_date();
                chatRoomListDto.getChatRoomRecentMessageDto().setMessage_date(localDateTime);

                // [unreadMessageCount] : 로그인 유저가 안 읽은 메시지 수는 (로그인 유저 & 채팅방)을 통해 조회하므로 (가능)
                chatRoomListDto.setUnreadMessageCount(unreadMessageCount);

                // [chatRoomProductDto] : 채팅 상대방이 구매자인 경우, 상품 정보를 활용할 수 있으므로 지정 (가능)
                chatRoomListDto.setChatRoomProductDto(modelMapper.map(chatRoom.getProduct(), ChatRoomProductDto.class));


            } else {
                // 3. 채팅 상대방 유저가 ~NULL (채팅 상대방이 삭제되지 않은 경우)


                // API 문서 업로드되면 응답 예시 수정
                /**
                 *  {
                 *    "roomId": 106,
                 *    "chatMessageUserDto":
                 *      {
                 *        "userId": 4,
                 *        "nickName": "b"
                 *      },
                 *    "chatRoomRecentMessageDto":
                 *      {
                 *        "message_type": 0,
                 *        "message_date": "2021-01-15T18:07:56",
                 *        "message": "안뇽"
                 *      },
                 *    "unreadMessageCount": 0,
                 *    "block": false,
                 *    "chatRoomProductDto":
                 *      {
                 *        "product_id": 5,
                 *        "product_deleted": 0,
                 *        "productImgPath": "/imgs/slideshow_sample.jpg"
                 *      }
                 *   }
                 * */

                BlockUser blockUser = blockUserRepository.findByUserAndBlockedUser(loginUser, opponentUser);

                // [roomId] : 채팅방 고유 ID 지정 (가능)
                chatRoomListDto.setRoomId(chatRoom.getRoomId());

                // [chatRoomRecentMessageDto] : 채팅방에서 주고 받은 마지막 메시지 (via 채팅방 고유 ID) (가능)
                chatRoomListDto.setChatRoomRecentMessageDto(modelMapper.map(recentMessage, ChatRoomRecentMessageDto.class));

                // [chatRoomRecentMessageDto] : 채팅방에서 주고 받은 마지막 메시지 (via 채팅방 고유 ID) (가능)
                LocalDateTime localDateTime = chatRoomListDto.getChatRoomRecentMessageDto().getMessage_date();
                chatRoomListDto.getChatRoomRecentMessageDto().setMessage_date(localDateTime);

                // [unreadMessageCount] : 로그인 유저가 안 읽은 메시지 수는 (로그인 유저 & 채팅방)을 통해 조회하므로 (가능)
                chatRoomListDto.setUnreadMessageCount(unreadMessageCount);

                // [block] : 채팅 상대방과 로그인 유저 간 차단 관계 파악 (가능)
                chatRoomListDto.setBlock(loginUser.IsBlock(blockUser));

                // [chatRoomProductDto] : 상품 정보를 활용할 수 있으므로 지정 (가능)
                chatRoomListDto.setChatRoomProductDto(modelMapper.map(chatRoom.getProduct(), ChatRoomProductDto.class));

                // [chatMessageUserDto] : 채팅 상대방 유저 정보를 활용 가능하므로 (가능)
                chatRoomListDto.setChatMessageUserDto(modelMapper.map(opponentUser, ChatMessageUserDto.class));

            }

            chatRoomListDtos.add(chatRoomListDto);


        } // for loop

        return chatRoomListDtos;
    }

    // 상품 상세 페이지 [채팅으로 거래하기] 버튼 클릭 시점
    // 채팅방 구독 처리
    // consumerId = 로그인 유저의 고유 ID (= 채팅으로 거래하기 버튼 클릭 -> 구매자 입장이므로)

    // [채팅으로 거래하기] 버튼 클릭 시, 상대방 유저가 삭제된 경우, AOP를 통해 미리 필터링이 되므로 여기서
    // 채팅 상대방이 삭제됨으로써 고려할 요소는 없다.
    /** 이 메소드를 호출하는 컨트롤러에 아직 AOP 미적용 상태 -> 논의 필요*/
    @Override
    public ChatRoomCheckExistedDto findChatRoomByProductSellerConsumer(int productId, int sellerId, int consumerId) {


        ChatRoom chatRoom = chatRoomRepository.findChatRoomsByProductIdAndSellerIdAndConsumerId(productId, sellerId, consumerId);

        ChatRoomCheckExistedDto chatRoomCheckExistedDto = new ChatRoomCheckExistedDto();

        if (chatRoom == null) {
            chatRoomCheckExistedDto.setRoom_existed(false);

        } else {
            chatRoomCheckExistedDto.setRoom_existed(true);
            chatRoomCheckExistedDto.setRoom_id(chatRoom.getRoomId());
        }
        return chatRoomCheckExistedDto;
    }

    // 채팅방 상단 Section 물건 정보 보여주기
    // 채팅 상대방이 해당 상품의 판매자 (= 상품 업로더)인 경우, 상품 정보까지 모두 삭제되므로, NPE 발생
    // -> 상품 삭제 됐을 경우 (= 해당 상품을 조회할 수 없는 경우), AOP(=ProductValidationForChatRoom)에서 필터링
    /** [B] 이 메소드를 호출하는 컨트롤러에 아직 AOP 적용 여부 확인 해야 함 -> 논의 필요 */
    @Override
    public ChatRoomSectionProductDto findRoomProductSectionByProduct(int productId, int userId) {


        Product product = productRepository.findById(productId);

        // 상품이 삭제되지 않은 상태
        return ChatRoomSectionProductDto.builder()
                .product_id(product.getId())
                .product_title(product.getTitle())
                .product_img_path(product.getImgDirectory())
                .product_price(product.getPrice())
                .transaction_status_id(product.getTransactionStatusId())
                .build();
    }

    /** [TESTED], 에러 메시지 수정 대상 */
    // 채팅방 물건 거래완료로 바꾸기
    @Override
    public void changeRoomTransactionStatus(int userId, int roomId, int status) {

        // 로그인 유저
        // 로그인 유저에 대해서 NPE 체크 또는 탈퇴 및 이용제재를 필터링하지 않는 이유
        // -> gateway 서버에서 처리가 됌.
        Member member = memberRepository.findById(userId);


        ChatRoom chatRoom = chatRoomRepository.getOne(roomId);
        // 물건 삭제 또는 블라인드 처리되었을 경우 불가
        /**[C] 에러 메시지 수정 대상 - 이 메시지 수정 시 다른 영향 가는 부분 확인하고 수정 할 예정 */
        // 1. 채팅방과 연결된 상품 정보가 삭제된 경우 (== null) --> 이 경우는 회원탈퇴로 인해 실제 데이터베이스에서 삭제됐을 경우이므로, 이미 Gateway에서 차단 처리 대상
        // 2. 채팅방과 연결된 상품이 업로더에 의해 삭제된 경우 (getProductStatus() == 1)
        if (chatRoom.getProduct().getProductStatus() == 1)
            throw new CustomControllerExecption("해당 중고물품은 삭제처리되었습니다.", HttpStatus.BAD_REQUEST, null, 100);

        if (chatRoom.getProduct().getProductStatus() == 2)
            throw new CustomControllerExecption("관리자에 의해 비공개 처리되어 거래완료를 할 수 없습니다.", HttpStatus.BAD_REQUEST, null, 101);

        // 채팅방의 물건 거래완료로 바꾸려는 데 채팅방에 있는 대화상대가 내가 차단한 경우라면 불가능
        // 채팅방 상대방이 사용자제재, 탈퇴 유저인지 확인하기

        // NPE (=채팅 상대방 유저가 탈퇴 이후, 실제 데이터베이스에서 데이터가 삭제된 경우)
        // 채팅 상대방 유저
        Member opponentMember = chatRoom.getMemberOpponent(member);

        // NPE 체크 -> 채팅 상대방 유저 정보를 확인해야 하는 하위 로직에 대한 NPE 발생을 차단
        if (opponentMember == null) {
            log.error("[데이터베이스에서 상대유저가 완전히 삭제된 이후] 탈퇴한 유저와 거래완료를 할 수 없습니다.");
            throw new CustomControllerExecption("탈퇴한 유저와 거래완료를 할 수 없습니다.", HttpStatus.BAD_REQUEST, null, 102);
        }

        if (opponentMember.getIsWithdrawn() == 1)
            throw new CustomControllerExecption("탈퇴한 유저와 거래완료를 할 수 없습니다.", HttpStatus.BAD_REQUEST, null, 102);


        if (opponentMember.getIsEnabled() == 1)
            throw new CustomControllerExecption("이용제재를 받고 있는 유저와 거래완료를 할 수 없습니다.", HttpStatus.BAD_REQUEST, null, 103);

        // 판매자가 '로그인' 유저인 경우 (= 거래상태를 '완료'로 바꾸는 요청은 '판매자만 가능')
        if (chatRoom.getSeller() == member) {

            // loginUser.getBlockUsers() : where user_id = loginUser.id
            // (= 로그인한 유저가 차단한 유저 리스트)
            BlockUser blockUser = blockUserRepository.findByUserAndBlockedUser(member, chatRoom.getConsumer());

            // loginUser.getUserBlockedMe() : where blocked_user_id = loginUser.id
            // (= 로그인한 유저를 차단한 리스트), 따라서 타겟 유저가 로그인 유저를 차단한 경우를 포함한다.
            BlockUser blockedUser = blockUserRepository.findByUserAndBlockedUser(chatRoom.getConsumer(), member);


            if (member.getBlockUsers().contains(blockUser)) {
                throw new CustomControllerExecption("차단한 유저와 거래완료를 할 수 없습니다.", HttpStatus.BAD_REQUEST, null, 104);
            }

            // 서로 차단 중이라면
            if (member.getUserBlockedMe().contains(blockedUser)) {
                throw new CustomControllerExecption("나를 차단한 유저와는 거래완료를 할 수 없습니다.", HttpStatus.BAD_REQUEST, null, 105);
            }

            // 이미 거래완료
            if (chatRoom.getProduct().getTransactionStatusId() == 2) {
                throw new CustomControllerExecption("이미 거래완료 되어 있는 상태입니다.", HttpStatus.BAD_REQUEST, null, 106);
            }

            if (chatRoom.getProduct().getTransactionStatusId() == 0) {
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
        } else { // 판매자 요청이 아닌 경우 '거래 완료'를 요청할 수 없다. ( 에초에 이런 경우가 있을 수 없다. )
            /**[C] 에러 메시지 수정 대상 - 이 메시지 수정 시 다른 영향 가는 부분 확인하고 수정 할 예정 */
//            throw new CustomControllerExecption("잘못된 요청입니다", HttpStatus.BAD_REQUEST, null);
        }
    }

    // 채팅방 삭제 (-> '채팅방 나가기' 클릭)
    // 채팅방 고유 ID는 회원탈퇴에도 영향 받지 않고 계속 남아 있는 데이터
    // 유저의 고유 ID는 회원탈퇴 시 삭제되는 값이지만, 탈퇴 상태인 경우 (물리 데이터 삭제 여부 상관 없이)
    // 채팅방 '나가기' 요청 시 Gateway 서버에서 필터링

    // 채팅 상대 유저가 탈퇴 상태인 경우와 무관
    // 채팅 상대가 방을 나가도 (삭제), 해당 채팅방에 접근 가능
    /** [A, 미구현] 회원탈퇴로 인해 유저 관련 정보 삭제 시, 채팅방 삭제 처리 (삭제 status 1로 변환) */
    @Override
    @Transactional
    public void leaveChatRoom(int roomId, int userId) {

        ChatRoom chatRoom = chatRoomRepository.getOne(roomId);
        if(chatRoom != null){
            chatRoom.leave(userId);
        }
    }

    // 채팅방 목록 -> '특정' 채팅방 클릭 시점
    // 채팅방의 상태조회
    /** [] 채팅 상대방이 탈퇴 후 데이터베이스에서 삭제된 경우, CustomControllerExecption Throw 했을 때 클라이언트 측 반응 안함 */
    @Override
    public ChatRoomStatusDto getChatRoomStatus(int roomId, int userId) {

        // 로그인 유저 (NPE 대상 X, Gateway 서버 필터링)
        Member member = memberRepository.findById(userId);

        // 채팅방 객체 (via ChatRoomId, NPE 대상 X)
        ChatRoom chatRoom = chatRoomRepository.getOne(roomId);

        // 로그인 유저기준 채팅방 상대 유저
        // NPE (= 채팅 상대방이 삭제된 경우)
        Member opponentUser = chatRoom.getMemberOpponent(member);

        if (opponentUser == null || opponentUser.getIsWithdrawn() == 1) {
            throw new CustomControllerExecption("탈퇴한 유저와 채팅거래를 할 수 없습니다.", HttpStatus.BAD_REQUEST, null, 102);
        }

        if (opponentUser.getIsEnabled() == 1) {
            throw new CustomControllerExecption("관리자로부터 이용제재를 받고 있는 유저와 채팅거래를 할 수 없습니다.", HttpStatus.BAD_REQUEST, null, 103);
        }

        // 로그인 유저 & 채팅 상대 유저 간 차단 여부 조회 (양방향)
        // loginUser.getBlockUsers() : where user_id = loginUser.id
        // (= 로그인한 유저가 차단한 유저 리스트)
        BlockUser blockUser = blockUserRepository.findByUserAndBlockedUser(member, opponentUser);

        // loginUser.getUserBlockedMe() : where blocked_user_id = loginUser.id
        // (= 로그인한 유저를 차단한 리스트), 따라서 타겟 유저가 로그인 유저를 차단한 경우를 포함한다.
        BlockUser blockedUser = blockUserRepository.findByUserAndBlockedUser(opponentUser, member);


        // 물건삭제되었을 경우
        if (chatRoom.getProduct().getProductStatus() == 1) {

            return ChatRoomStatusDto.builder()
                    .productStatus(PRODUCT_STATUS_PRODUCT_DELETE) // 3
                    .isWarn(opponentUser.checkWarnActive()) // 경고유무
                    .block_status(member.checkBlockStatus(blockUser, blockedUser)) // 차단상태
                    .build();
        }

        // 물건 비공개 처리 되었을 경우
        if (chatRoom.getProduct().getProductStatus() == 2) {
            return ChatRoomStatusDto.builder()
                    .productStatus(PRODUCT_STATUS_PRODUCT_BLIEND) // 4
                    .isWarn(opponentUser.checkWarnActive()) // 경고유무
                    .block_status(member.checkBlockStatus(blockUser, blockedUser)) // 차단상태
                    .build();
        }

        // ProductReview -> 해당 물품이 판매 완료된 시점 이후
        // ProductReview (NPE 대상, 작성자 또는 작성 대상자(= 작성자가 구매한 상품 판매자)가 탈퇴 후 데이터 삭제되는 경우, 해당 후기글 삭제)
        // 위 경우, 채팅방 고유 ID가 있음에도 해당 ID로 후기(ProductReview) 조회 시 NULL 반환
        // 단, 위에서 채팅 상대방 유저가 탈퇴 후 삭제된 경우 미리 예외처리에서 필터링 되므로, 이곳에서 따로 처리할 부분은 없음.
        Optional<ProductReview> productReview = productReviewRepository.findByChatRoom(chatRoom);

        if (productReview.isPresent()) { // 판매자가 판매를 하였을 경우

            if (productReview.get().getConsumer() == member){ // 구매자
                ChatRoomStatusDto chatRoomStatusDto = ChatRoomStatusDto.builder()
                        .productStatus(PRODUCT_STATUS_CONSUMER)
                        .transactionStatus(SOLD)
                        .isWarn(opponentUser.checkWarnActive()) // 경고유무
                        .block_status(member.checkBlockStatus(blockUser, blockedUser)) // 차단상태
                        .build();
                if (productReview.get().getReviewMessage() == null) {
                    chatRoomStatusDto.setIsReviewUpload(NO);
                } else {
                    chatRoomStatusDto.setIsReviewUpload(YES);
                }
                return chatRoomStatusDto;

            } else {  // 판매자

                String reviewerNickname  = productReview.get().getConsumer().getNickName();
                ChatRoomStatusDto chatRoomStatusDto = ChatRoomStatusDto.builder()
                        .productStatus(PRODUCT_STATUS_SELLER)
                        .transactionStatus(SOLD)
                        .reviewer_nickname(reviewerNickname)
                        .isWarn(opponentUser.checkWarnActive()) // 경고유무
                        .block_status(member.checkBlockStatus(blockUser, blockedUser)) // 차단상태
                        .build();

                if (productReview.get().getReviewMessage() == null) { // 거래완료 & 구매자 후기 아직 안남김
                    chatRoomStatusDto.setIsReviewUpload(NO);
                } else {
                    chatRoomStatusDto.setIsReviewUpload(YES); // 구매후기 있고
                }
                return chatRoomStatusDto;

            }


        } else {  // 판매자가 구매완료 버튼 누르지 않은 경우 ( 모두에게 공평 ) + 구매버튼을 눌러도 해당 거래완료한 해당 채팅방이 아닐경우

            if(chatRoom.getSeller() == member) { // 물건 올린 사람

                if(chatRoom.getProduct().getTransactionStatusId() == 2) { // 이미 다른 곳에서 판매자가 거래완료 함
                    return ChatRoomStatusDto.builder()
                            .productStatus(PRODUCT_STATUS_SELLER)
                            .transactionStatus(SOLD_BY_ANOTHER_ROOM)
                            .isWarn(opponentUser.checkWarnActive()) // 경고유무
                            .block_status(member.checkBlockStatus(blockUser, blockedUser)) // 차단상태
                            .build();
                }

                return ChatRoomStatusDto.builder()
                        .productStatus(PRODUCT_STATUS_SELLER)
                        .transactionStatus(SOLDNOT)
                        .isWarn(opponentUser.checkWarnActive()) // 경고유무
                        .block_status(member.checkBlockStatus(blockUser, blockedUser)) // 차단상태
                        .build();
            } else {
                return ChatRoomStatusDto.builder()
                        .productStatus(PRODUCT_STATUS_ETC)
                        .isWarn(opponentUser.checkWarnActive()) // 경고유무
                        .block_status(member.checkBlockStatus(blockUser, blockedUser)) // 차단상태
                        .build();
            }
        }
    }


    // 채팅방 별 유저 채팅방 입장 시간 기준으로 읽지 않은 총 메시지의 개수 계산
    // 파라미터 (Chatroom, loginUser -> NPE 대상 X)
    public Long calculateUnreadMessageCount(ChatRoom chatRoom, Member loginUser){

        // getUsersEntranceDate(Member loginUser) : 로그인 유저가 해당 채팅방에서 구매자인 지 판매자인 지 판단 후, 입장 시간 값 반환
        LocalDateTime usersEntranceDate = chatRoom.getUsersEntranceDate(loginUser);

        // getUserleaveDate(Member loginUser) : 로그인 유저가 채팅방 나갈 때 요청 -> 로그인 유저가 구매자인지 판매자인지 체크 후 '채팅방 나가기' 클릭한 시점 반환
        LocalDateTime usersRoomDeletedDate = chatRoom.getUserleaveDate(loginUser);

        // 채팅방 삭제 -> [채팅방 옵션에서 채팅방 나가기 클릭]
        // 로그인 유저가 채팅방 입장했었고, 해당 채팅방을 삭제하지 않은 경우
        if (usersEntranceDate != null && usersRoomDeletedDate == null) {
            log.info("[로그인 유저의 읽지 않은 메시지 수 조회] 로그인 유저가 채팅방 입장했었고, 해당 채팅방을 삭제하지 않은 경우");
            Long countUserUnreadMsgAfterEntranceDate = chatMessageRepository.countAllByRoomIdAndMsgDateIsAfterAndReceiverAndMsgStatus(chatRoom.getRoomId(), usersEntranceDate, loginUser, 0);
            return countUserUnreadMsgAfterEntranceDate;
        }

        // 로그인 유저(=판매자인 경우), 생성된 채팅방에 입장하지 않은 상태이고, 해당 채팅방을 삭제하지 않은 경우
        else if (usersEntranceDate == null && usersRoomDeletedDate == null) {
            log.info("[로그인 유저의 읽지 않은 메시지 수 조회] 로그인 유저(=판매자인 경우), 생성된 채팅방에 입장하지 않은 상태이고, 해당 채팅방을 삭제하지 않은 경우");
            Long countUserUnreadMsgAfterEntranceDate = chatMessageRepository.countAllByRoomIdAndReceiverAndMsgStatus(chatRoom.getRoomId(), loginUser, 0);
            return countUserUnreadMsgAfterEntranceDate;
        }

        // 로그인 유저(=판매자인 경우), 생성된 채팅방에 입장하지 않은 상태로 해당 채팅방을 삭제한 경우
        else if (usersEntranceDate == null && usersRoomDeletedDate != null) {
            log.info("[로그인 유저의 읽지 않은 메시지 수 조회] 로그인 유저(=판매자인 경우), 생성된 채팅방에 입장하지 않은 상태로 해당 채팅방을 삭제한 경우");
            Long countUserUnreadMsgAfterEntranceDate = chatMessageRepository.countAllByRoomIdAndMsgDateIsAfterAndReceiverAndMsgStatus(chatRoom.getRoomId(), usersRoomDeletedDate, loginUser, 0);
            return countUserUnreadMsgAfterEntranceDate;
        }

        // 로그인 유저가 입장했던 방이지만, 해당 채팅방에서 나간 경우
        else {
            log.info("[로그인 유저의 읽지 않은 메시지 수 조회] 로그인 유저가 입장했던 방이지만, 해당 채팅방에서 나간 경우");
            log.info("[예 - 로그인 유저가 입장했다가 나간 방인데, 나간 이후 상대방이 메시지를 전송한 경우]");

            // 채팅방 재입장 시간 < 채팅방 나간 시간
            // 채팅방 나간 시간이 큰 경우, 채팅방 나간 시간 기준으로 안 읽은 시간을 조회한다.
            if (usersRoomDeletedDate.isAfter(usersEntranceDate)) {
                log.info("[채팅방 재입장 시간 < 채팅방 나간 시간]");
                Long countMessageAfterUserDeletedRoom = chatMessageRepository.countAllByRoomIdAndMsgDateIsAfterAndReceiverAndMsgStatus(chatRoom.getRoomId(), usersRoomDeletedDate, loginUser, 0);
                return countMessageAfterUserDeletedRoom;
            }
            // 채팅방 재입장 시간 > 채팅방 나간 시간
            // 재입장 시간이 큰 경우, 입장 시간 기준으로 안 읽은 메시지를 조회한다.
            else {
                log.info("[채팅방 재입장 시간 > 채팅방 나간 시간]");
                Long countUserUnreadMsgAfterEntranceDate = chatMessageRepository.countAllByRoomIdAndMsgDateIsAfterAndReceiverAndMsgStatus(chatRoom.getRoomId(), usersEntranceDate, loginUser, 0);
                return countUserUnreadMsgAfterEntranceDate;
            }
        }
    }


    // 상품 상세화면에서 [채팅으로 거래하기] 클릭 시점
    // 채팅방 나간 상태 값 업데이트 로직 추가

    /** 2021-05-28 테스트 by TK
     *
     * [] 상대 유저 탈퇴 또는 이용제재 상태인 경우 -> Alert("해당 중고물품은 회원님과 거래한 적이 있습니다") -> 해당 채팅 화면 이동 -> "이름 없음" (채팅방, 내용은 조회 가능)
     *
     * [] 해당 상품의 상태가 1 (= 삭제)인 경우 -> 404 응답은 확인 했는데, 클라이언트 측에서 반응 안 함
     *
     * [] 해당 상품의 상태가 2 (= 비공개)인 경우 -> 404 응답은 확인 했는데, 클라이언트 측에서 반응 안 함
     *
     * [] 해당 상품의 판매자가 회원탈퇴 후 데이터베이스에서 완전히 삭제된 경우  -> 404 응답은 확인 했는데, 클라이언트 측에서 반응 안 함
     *
     * [] 해당 상품의 판매자가 로그인 유저를 차단한 경우 -> Alert("해당 중고물품은 회원님과 거래한 적이 있습니다") -> 메시지 입력 시점에 차단으로 인해 채팅 불가 안내
     *
     * [] 로그인 유저가 해당 상품의 판매자를 차단한 경우 -> Alert("해당 중고물품은 회원님과 거래한 적이 있습니다") -> 메시지 입력 시점에 차단으로 인해 채팅 불가 안내
     *
     *
     *
     * */


    @Override
    public ChatRoomTradeHistoryDto checkChatHistory(int userId, int productId) {

        // 상품 정보는 로그인 유저가 판매자가 아닌 경우 -> NPE 대상 (채팅 상대방이 판매자이고, 탈퇴 후 삭제되는 경우 상품 정보가 삭제)
        // 채팅 상세화면에서 [채팅으로 거래하기] 클릭하는 유저는 구매자이므로, 항상 NPE 대상이 되는 상황
        Product product = productRepository.findById(productId);
        // 상품 정보 NPE 체크
        // 채팅 상대방 (= 상품 판매자)이 해당 상품 게시물을 삭제한 경우 -> 존재하지 않는 물건입니다. 예외처리 (상품 판매자 정보는 있지만, 상품 정보가 NULL)
        // 채팅 상대방 (= 상품 판매자)이 삭제됐으므로, 상품 정보가 NULL -> 존재하지 않는 물건입니다. 예외처리 (상품 판매자 정보가 NULL 인 경우 내포)
        if (product == null) throw new CustomControllerExecption("존재하지 않은 물건입니다.", HttpStatus.BAD_REQUEST, null, 100);


        // 상품 정보, 판매자 정보, 로그인 유저 정보를 기반으로 생성된 채팅방 정보 있는 지 확인
        // 상품 정보, 판매자 정보 (-> NPE 대상)
        // 상품 정보가 NULL 인 경우 위 조건문에서 체크 되므로 파라미터 값 검증된 상태)
        ChatRoom chatRoom = chatRoomRepository.findChatRoomsByProductIdAndSellerIdAndConsumerId(productId, product.getMember().getId(), userId);

        if (chatRoom != null) {
            Member loginUser = memberRepository.findById(userId);

            ChatRoomTradeHistoryDto chatRoomTradeHistoryDto = ChatRoomTradeHistoryDto.builder()
                    .history_product_id(chatRoom.getProduct().getId())
                    .history_room_id(chatRoom.getRoomId())
                    .isExisted(true)
                    .build();

            // 로그인 유저가 해당 채팅방의 판매자로서 있는 경우
            if (chatRoom.isMine(loginUser)) {
                // 채팅방 나간 상태인 경우 -> 나감 상태 처리 (객체의 상태 값 변경, update)
                if (chatRoom.getSellerDeleted() == 1) {
                    chatRoomTradeHistoryDto.setLeave(true);
                }
            } else { // 로그인 유저가 해당 채팅방의 구매자로 있는 경우
                if (chatRoom.getConsumerDeleted() == 1) {
                    // 채팅방 나간 상태인 경우 -> 나감 상태 처리(객체의 상태 값 변경, update)
                    chatRoomTradeHistoryDto.setLeave(true);
                }
            }
            return chatRoomTradeHistoryDto;
        }

        // (물건삭제, 물건블라인드), 유저제재, 유저탈퇴, 유저차단
        if (product.getProductStatus() == 1) throw new CustomControllerExecption("삭제된 중고물품의 경우 채팅거래를 하실 수 없습니다.", HttpStatus.BAD_REQUEST, null, 100);
        if (product.getProductStatus() == 2) throw new CustomControllerExecption("관리자에 의해 비공개 처리된 물건입니다. 채팅거래를 하실 수 없습니다.", HttpStatus.BAD_REQUEST, null, 101);
        if (product.getMember().getIsWithdrawn() == 1) throw new CustomControllerExecption("탈퇴한 유저입니다. 채팅거래를 하실 수 없습니다.", HttpStatus.BAD_REQUEST, null, 102);
        if (product.getMember().getIsEnabled() == 1) throw new CustomControllerExecption("관리자로부터 이용제재당한 유저와 채팅거래를 하실 수 없습니다.", HttpStatus.BAD_REQUEST, null, 103);

        // 로그인 유저
        Member loginUser = memberRepository.findById(userId);
        // 상품 업로드한 유저 (NPE 대상이지만, 필터링 됨)
        // 상품 -> 멤버 객체탐색 (상품 정보를 통해 해당 업로더의 고유 ID 조회)
        Member productUploader = memberRepository.findById(product.getMember().getId());

        // NPE 체크 불필요 (아래 이 객체 활용 로직이 contains() 함수 = null도 인자로 활용가능)
        // 로그인한 유저가 차단한 리스트에 상품 업로드의 상품이 있는 지 또는
        // 로그인한 유저를 차단한 리스트에 해당 상품 업로더가 있는 지 체크

        // loginUser.getBlockUsers() : where user_id = loginUser.id
        BlockUser blockUser = blockUserRepository.findByUserAndBlockedUser(loginUser, productUploader);

        // loginUser.getUserBlockedMe() : where blocked_user_id = loginUser.id
        BlockUser blockedUser = blockUserRepository.findByUserAndBlockedUser(productUploader, loginUser);

        // 로그인 유저를 기준으로 차단한 리스트에 상품 업로더의 고유 아이디가 포함된 경우
        // (= 상품 정보를 조회 했는데, 로그인 유저가 차단한 유저의 상품인 경우)
        // (= 채팅거래 진행 못하는 경우)

        // 각 예외 조건의 우선순위는
        // 1. 로그인 유저가 차단한 유저의 상품인 지 체크
        // 2. 상품의 업로더가 로그인 유저를 차단했는 지 체크

        // 1. 로그인 유저가 차단한 유저의 상품인 지 체크
        if (loginUser.getBlockUsers().contains(blockUser)) {
            throw new CustomControllerExecption("차단한 유저와는 채팅 거래 할 수 없습니다.", HttpStatus.BAD_REQUEST, null, 104);
        }

        // 2. 상품의 업로더가 로그인 유저를 차단했는 지 체크
        if (loginUser.getUserBlockedMe().contains(blockedUser)) {
            throw new CustomControllerExecption("나를 차단한 유저와는 채팅 거래 할 수 없습니다.", HttpStatus.BAD_REQUEST, null, 105);
        }

        return ChatRoomTradeHistoryDto.builder()
                .isExisted(false).build();
    }
}
