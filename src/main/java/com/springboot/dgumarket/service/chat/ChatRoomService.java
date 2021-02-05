package com.springboot.dgumarket.service.chat;

import com.springboot.dgumarket.dto.chat.*;
import com.springboot.dgumarket.model.chat.ChatRoom;
import com.springboot.dgumarket.model.member.Member;

import java.util.List;

/**
 * Created by MS KIM
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 *
 * addProductComment -> 거래 후기 남기기(구매자)
 * getProductComment -> 거래 후기 보기(구매자, 판매자)
 */

public interface ChatRoomService {
    List<ChatRoomListDto> findAllRoomsByUserId(int userId); // 모든 채팅방목록가져오기
    ChatRoomCheckExistedDto findChatRoomByProductSellerConsumer(int productId, int sellerId, int consumerId); // 채팅방 존재 유무 찾기
    ChatRoomSectionProductDto findRoomProductSectionByProduct(int productId, int userId);// 물건번호로 물건 정보 가져오기
    boolean changeRoomTransactionStatus (int roomId, int status); // 채팅방 거래상태 바꾸기
    ChatRoomStatusDto getChatRoomStatus(int roomId, int userId); // 채팅방상태 조회하기
    void leaveChatRoom(int roomId, int userId); // 채팅방 나가기
    Long calculateUnreadMessageCount(ChatRoom room, Member member); // 개별 채팅방의 읽지않은 메시지개수 계산하기
}
